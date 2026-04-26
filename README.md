<h1 style="text-align: left; display: flex;">
  <img src="https://raw.githubusercontent.com/Melikash98/Haus-Suche/main/logoApp.png" alt="Logo" width="80px"   height="80px" style="margin-right: 10px;padding-top: 6rem;" />
Haus Suche
</h1>

[![](https://jitpack.io/v/Foysalofficial/NafisBottomNav.svg)](https://jitpack.io/#Foysalofficial/NafisBottomNav)
[![](https://jitpack.io/v/mhiew/AndroidPdfViewer.svg)](https://jitpack.io/#mhiew/AndroidPdfViewer)
[![](https://jitpack.io/v/koral--/android-gif-drawable.svg)](https://jitpack.io/#koral--/android-gif-drawable)
[![](https://jitpack.io/v/cloudinary/cloudinary_android.svg)](https://jitpack.io/#cloudinary/cloudinary_android)


<img src="https://raw.githubusercontent.com/Melikash98/Haus-Suche/main/ezgif.com-animated-gif-maker.gif" alt="Logo" width="100%"   height="100%" style="margin-right: 10px;padding-top: 6rem;" />
##
<p align="left" width="100%">
  <strong>Discover and book beautiful villas and holiday homes for your next getaway.</strong>
  <em> Fast search, real photos, and easy booking — your perfect stay is just a tap away.</em>
</p>


---
<p align="left">
  <a href="#features">Features</a><br>
   <a href="#tech-stack">Tech Stack</a><br>
  <a href="#contributing">Contributing</a><br>
   <a href="#license">License</a><br>
</p>


---

## Overview

**Haus Suche**
is a lightweight Android application focused on property discovery and short-term bookings. The app demonstrates common mobile patterns—structured categories, multi-criteria filters, a three-feed Explore section (Top Picks / Nearby / Latest), and an integrated communication flow for contacting property owners. The implementation prioritizes real-time data sync and privacy-preserving communication while keeping the codebase simple and approachable for learning and iteration.

This repository is a practical junior-level project intended for portfolio/demo use. Code is organized to show modular UI components, Firebase-backed data flows, and typical Android UI patterns (RecyclerView, Intent-based actions, modular data layer).

---

## Features <a name="features"></a>
<ul>
  <li>Smart discovery — categories + advanced filters (price, distance, amenities) to quickly narrow results.</li>
  <li>Explore feeds — three dedicated RecyclerView feeds: Top Picks, Nearby, and Latest.</li>
  <li>Favorites & history — bookmark listings and keep a lightweight activity history for basic personalization.</li>
  <li>Secure in-app email relay — messages are routed through the platform so replies show inside the app without exposing raw contact details.</li>
  <li>Multiway authentication — email/password plus social sign-in options.</li>
  <li>Real-time updates — listings and metadata update instantly for a responsive UX.</li>
  <li>Document verification (optional) — host/guest verification flow to increase trust.</li>
  <li>In-app helpers & notifications — contextual tips and push notifications to guide users.</li>
</ul>

---

## Tech Stack <a name="tech-stack"></a>
<ul>
  <li>Android (Java) — project UI & app logic.</li>
  <li>Realtime backend: Firebase.</li>
  <li>Source & hosting: GitHub.</li>
  <li>Social auth options: Google and Facebook.</li>
</ul>
 (Keep service keys and credentials out of the repository — use google-services.json locally or environment variables for CI.)
---
## Contributing <a name="contributing"></a>

Contributions are welcome — this repo is intentionally simple to make it easy to read and extend. Suggested contributions:
<ul>
  <li>Fix UI bugs or improve layouts for multiple screen sizes.</li>
  <li>Add unit and instrumentation tests for critical flows.</li>
  <li>Improve documentation (setup steps, architecture diagram).</li>
  <li>Implement additional Explore ranking signals or offline caching.</li>
</ul>
If you want to contribute, please open an issue describing the change, then send a small PR focused on one task. Keep changes minimal and well-documented.

---

## License <a name="license"></a>

<p align="left" width="100%">
  This project does not include a license file by default. For portfolio/demo use, consider adding a permissive license such as MIT. If you want, I can add a proper LICENSE file and the short license header to the top of main source files
</p>

---
