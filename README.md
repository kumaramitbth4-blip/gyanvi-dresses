# GYANVI DRESSES — Multi-Vendor E-Commerce Marketplace (Android)

**GYANVI DRESSES** is a full-featured Kotlin & Jetpack Compose Material 3 multi-vendor fashion marketplace Android application supporting **Buyer**, **Seller**, and **Super Admin** workflows.

## Key Modules Implemented
1. **Frontend & Responsive UI:** Light Yellow & Pink branding + Reactive **Dark Mode**, Swipeable Promotional Banner Slider, Multi-language selector (`English`, `हिंदी`, `मराठी`, and more), Voice Search, Camera/Image Search, 4-Way Product Filters, and Bottom Navigation.
2. **Buyer Panel:** Wishlist, Shopping Cart, Multi-Gateway Checkout (Google Pay, PhonePe, UPI ID, Card, Net Banking, Cash on Delivery), **Gyanvi Wallet & Reward Points**, Order Tracking, Cancel/Return Refund flow, Side-by-Side Dress Comparison, and **GST Tax Invoice** viewer.
3. **Seller Panel & Clothing Management:** Seller KYC Registration, Single & Bulk Product Upload, Clothing Sizes (`XS`–`7XL`, Numeric `0`–`50`, Waist `24`–`50`, Kids Age-Years), Multi-Color Swatches, Manufacturer & Packer Pincode/Address compliance, Barcode/QR & Shipping Label generation, Inventory, and Payouts.
4. **Super Admin Dashboard (9 Tabs):** Overview, Catalog QC Approvals, Seller Approval, Order Management, Seller Payouts, Revenue & P&L Analytics Charts, Buyers Block/Unblock, Coupon & Banner/Category Manager, Refunds/RTO/Complaints, RBAC Matrix, Audit Logs, and Live Broadcast Notifications.
5. **Notifications, AI & Security:** Category-filtered Notification Inbox & Channel Settings (Push, WhatsApp, SMS, Email), AI Fashion Stylist & Size Fit + Live Seller Chat, 2FA/OTP Dispatcher, Fraud Shield, and **15-Module Weekly Development Progress & Tap-to-Set-Up Hub**.

## Tech Stack
- **Language & UI:** Kotlin, Jetpack Compose (Material 3)
- **Architecture:** MVVM with `ViewModel`, Kotlin Coroutines & `StateFlow`
- **Min / Target SDK:** `minSdk = 24`, `targetSdk = 36`, Java 11
- **Secrets Management:** Configured via `.env` / `.env.example` (`Secrets Gradle Plugin`) — never commit real `.env` credentials to GitHub.

## Local Build & Testing
```bash
gradle :app:assembleDebug
gradle :app:testDebugUnitTest
```
