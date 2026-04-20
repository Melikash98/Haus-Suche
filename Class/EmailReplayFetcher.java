package com.melikash98.housesuche.Email;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.melikash98.housesuche.HelperClass.NotificationHelper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.search.AndTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

public class EmailReplayFetcher {
    private static final String TAG = "EMAIL_REPLAY";
    private static final String IMAP_HOST = "imap.gmail.com";
    private static final int IMAP_PORT = 993;
    private static final String APP_EMAIL = "Melika.sh.pc@gmail.com";
    private static final String APP_PASSWORD = "mgxllmodvxgjwtpw";

    public interface Callback {
        void onSuccess(int repliesProcessed);

        void onError(String error);
    }

    public static void fetchAndProcessReplies(Callback callback) {
        fetchAndProcessReplies(null, null, callback);
    }

    public static void fetchAndProcessReplies(Context context, String currentUserUid, Callback callback) {
        new Thread(() -> {
            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");
            props.put("mail.imap.ssl.enable", "true");
            props.put("mail.imap.host", IMAP_HOST);
            props.put("mail.imap.port", String.valueOf(IMAP_PORT));

            try {
                Session session = Session.getDefaultInstance(props);
                Store store = session.getStore("imaps");
                store.connect(IMAP_HOST, APP_EMAIL, APP_PASSWORD);

                Folder inbox = store.getFolder("INBOX");
                inbox.open(Folder.READ_WRITE);

                // Message[] messages = inbox.search(new SubjectTerm("Ref:"));

                SearchTerm unseenTerm = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
                SearchTerm subjectTerm = new SubjectTerm("Ref:");
                Message[] messages = inbox.search(new AndTerm(unseenTerm, subjectTerm));

                int processed = 0;
                for (Message msg : messages) {
                    String from = (msg.getFrom() != null && msg.getFrom().length > 0)
                            ? msg.getFrom()[0].toString().toLowerCase() : "";

                    if (from.contains("melika.sh.pc@gmail.com")) {
                        msg.setFlag(Flags.Flag.SEEN, true);
                        continue;
                    }
                    String subject = msg.getSubject();
                    if (subject == null || !subject.contains("Ref:")) continue;

                    String itemId = extractInquiryId(subject);
                    if (itemId == null) {
                        Log.w(TAG, "Could not extract itemId from subject: " + subject);
                        continue;
                    }
                    String inquiryId = extractInquiryId(subject);
                    if (inquiryId == null) continue;

                    String fullBody = getTextFromMessage(msg);
                    String replyText = extractReplyText(fullBody);

                    from = msg.getFrom()[0].toString();
                    Date sentDate = msg.getSentDate();

                    final String[] fromHolder = {from};
                    final Date[] sentDateHolder = {sentDate};

                    DatabaseReference emailRef = FirebaseDatabase.getInstance()
                            .getReference("Emails")
                            .child(inquiryId);
                    emailRef.child("ownerPhoto").get().addOnCompleteListener(photoTask -> {
                        String ownerPhotoUrl = "";

                        if (photoTask.isSuccessful() && photoTask.getResult().exists()) {
                            ownerPhotoUrl = photoTask.getResult().getValue(String.class);
                        }
                        if (ownerPhotoUrl == null || ownerPhotoUrl.isEmpty()) {
                            try {
                                if (msg.getFrom() != null && msg.getFrom().length > 0) {
                                    InternetAddress fromAddress = (InternetAddress) msg.getFrom()[0];
                                    String ownerEmail = fromAddress.getAddress() != null
                                            ? fromAddress.getAddress().trim().toLowerCase() : "";
                                    ownerPhotoUrl = getGravatarUrl(ownerEmail);
                                }
                            } catch (Exception e) {
                                Log.w(TAG, "Fallback photo failed", e);
                                ownerPhotoUrl = "https://www.gravatar.com/avatar/?s=200&d=mp&r=g";
                            }
                        }
                        saveReplyToFirebase(inquiryId, replyText, fromHolder[0], ownerPhotoUrl, sentDateHolder[0]);
                    });
                    Log.d("EMAIL_REPLAY", "New unread reply found! inquiryId=" + inquiryId + " | from=" + from);
                    if (context != null && currentUserUid != null) {
                        Log.d("EMAIL_REPLAY", "Calling showReplyNotification...");
                        NotificationHelper.showReplyNotification(context,
                                "Neue Stellungnahme des Grundstückseigentümers",
                                "Sie haben eine neue Antwort vom Inhaber erhalten.");
                    } else {
                        Log.w("EMAIL_REPLAY", "Cannot show notification: context or userUid is null");
                    }
                    /*try {
                        if (msg.getFrom() != null && msg.getFrom().length > 0) {
                            InternetAddress fromAddress = (InternetAddress) msg.getFrom()[0];
                            String ownerEmail = fromAddress.getAddress() != null
                                    ? fromAddress.getAddress().trim().toLowerCase()
                                    : "";
                            ownerPhotoUrl = getGravatarUrl(ownerEmail);
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Failed to get photo", e);
                    }*/

                    msg.setFlag(Flags.Flag.SEEN, true);
                    processed++;
                }

                inbox.close(false);
                store.close();

                if (callback != null) callback.onSuccess(processed);
            } catch (Exception e) {
                Log.e(TAG, "Error fetching replies", e);
                if (callback != null) callback.onError(e.getMessage());
            }
        }).start();
    }

    private static String extractInquiryId(String subject) {
        int refIndex = subject.indexOf("Ref:");
        if (refIndex == -1) return null;
        String after = subject.substring(refIndex + 4).trim();
        int end = after.indexOf(")");
        return (end != -1) ? after.substring(0, end).trim() : after.trim();
    }

    private static String getTextFromMessage(Message msg) throws Exception {
        if (msg.isMimeType("text/plain")) {
            return (String) msg.getContent();
        }
        if (msg.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) msg.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    return (String) bp.getContent();
                }
            }
        }
        return "";
    }

    private static String extractReplyText(String fullBody) {
        if (fullBody == null || fullBody.trim().isEmpty()) return "";

        String[] markers = {
                "On ",
                "-----Original Message-----",
                "-------- Original Message --------",
                "From:",
                "Sent:",
                "To:"
        };

        for (String marker : markers) {
            int idx = fullBody.indexOf(marker);
            if (idx > 5) {
                String reply = fullBody.substring(0, idx).trim();
                if (!reply.isEmpty()) {
                    return reply;
                }
            }
        }
        return fullBody.trim();
    }

    private static void saveReplyToFirebase(String inquiryId, String replyText, String fromOwner, String ownerPhotoUrl, Date sentDate) {
        DatabaseReference globalRef = FirebaseDatabase.getInstance().getReference("Emails").child(inquiryId);
        globalRef.child("userUid").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String uid = task.getResult().getValue(String.class);
                if (uid != null) {
                    DatabaseReference inquiryRef = FirebaseDatabase.getInstance()
                            .getReference("Users")
                            .child(uid)
                            .child("inquiries")
                            .child(inquiryId).child("replies").push();
                    String replyId = inquiryRef.getKey();

                    Map<String, Object> replyData = new HashMap<>();
                    replyData.put("replyId", replyId);
                    replyData.put("inquiryId", inquiryId);
                    replyData.put("replyText", replyText);
                    replyData.put("fromOwner", fromOwner);
                    replyData.put("ownerPhotoUrl", ownerPhotoUrl);
                    replyData.put("ownerEmail", fromOwner);
                    replyData.put("isOwnerReply", true);
                    replyData.put("replayDate", sentDate.getTime());
                    replyData.put("receivedAt", ServerValue.TIMESTAMP);
                    replyData.put("isRead", false);


                    inquiryRef.setValue(replyData);
                    saveNewReplyNotification(inquiryId, replyId);
                    Log.d(TAG, "Reply saved for inquiry: " + inquiryId + " | replyId: " + replyId);
                }
            }
        });
    }

    private static void saveNewReplyNotification(String inquiryId, String replyId) {
        DatabaseReference emailsRef = FirebaseDatabase.getInstance()
                .getReference("Emails").child(inquiryId);

        emailsRef.child("userUid").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || !task.getResult().exists()) return;

            String uid = task.getResult().getValue(String.class);
            if (uid == null) return;

            DatabaseReference notifRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(uid)
                    .child("MessageNotification")
                    .push();

            Map<String, Object> notification = new HashMap<>();
            notification.put("message", "Sie haben eine neue Antwort vom Eigentümer der Immobilie erhalten.:\n");
            notification.put("isRead", false);
            notification.put("timestamp", ServerValue.TIMESTAMP);
            notification.put("type", "NEW_REPLY");
            notification.put("replyId", replyId);
            notification.put("inquiryId", inquiryId);

            notifRef.setValue(notification).addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Notification saved for inquiry: " + inquiryId);
                updateUnreadReplyCount(uid);
            });
        });
    }

    private static void updateUnreadReplyCount(String uid) {
        if (uid == null) return;

        DatabaseReference notificationsRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid)
                .child("MessageNotification");

        notificationsRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || !task.getResult().exists()) {
                FirebaseDatabase.getInstance()
                        .getReference("Users").child(uid)
                        .child("unreadReplyCount").setValue(0);
                return;
            }

            long unreadCount = 0;
            for (DataSnapshot snapshot : task.getResult().getChildren()) {
                String type = snapshot.child("type").getValue(String.class);
                Boolean read = snapshot.child("read").getValue(Boolean.class);

                if ("NEW_REPLY".equals(type) && read != null && !read) {
                    unreadCount++;
                }
            }

            FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(uid)
                    .child("unreadReplyCount")
                    .setValue(unreadCount);
        });
    }

    private static String getGravatarUrl(String email) {
        if (email == null || email.trim().isEmpty()) {
            return "https://www.gravatar.com/avatar/?s=200&d=mp&r=g";
        }
        String emailClean = email.trim().toLowerCase();
        String hash = md5Hash(emailClean);
        return "https://www.gravatar.com/avatar/" + hash + "?s=200&d=mp&r=g";
    }

    private static String md5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            Log.e(TAG, "MD5 error", e);
            return "";
        }
    }
}
