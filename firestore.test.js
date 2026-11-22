const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_seller_123";
const BOB_UID = "bob_buyer_456";
const ADMIN_UID = "admin_gyanvi_789";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function buildValidOrderPayload(uid, orderId, overrides = {}) {
  const past = new Date(Date.now() - 5000);
  return {
    userId: uid,
    orderId,
    productId: "gd_01",
    productName: "Jaipur Block Print Kurti",
    productCategory: "Kurtis & Suits",
    productPrice: 599,
    productImageRes: 1001,
    orderDate: "17 Nov 2026",
    status: "ORDERED",
    statusText: "New Order Received • Pending",
    deliveryDateRange: "Mon, 23 Nov - Wed, 25 Nov",
    selectedSize: "38 (M)",
    quantity: 1,
    totalAmount: 599,
    trackingStages: [
      {
        title: "Order Placed",
        subtitle: "Pending seller confirmation",
        completed: true,
        dateStr: "17 Nov",
      },
    ],
    userRating: 0,
    userReviewText: "",
    feedbackCompleted: false,
    paymentMethod: "UPI (PhonePe)",
    awbNumber: "DLV-845438912",
    customerName: "Ritu Singh",
    deliveryAddress: "Boring Road, Near Crossing",
    labelDownloaded: false,
    sellerStage: "Pending",
    customerPhone: "+91 9661209871",
    customerAgeYears: "25 Years",
    selectedColor: "Jaipur Pink",
    paymentStatus: "Paid",
    orderConfirmed: false,
    qualityVerified: false,
    packed: false,
    invoiceGenerated: false,
    barcodeScanned: false,
    courierPartner: "Delhivery Express",
    pickupStatus: "Not Requested",
    shippingCharges: 49,
    expectedDeliveryDate: "23 Nov 2026",
    currentTrackingLocation: "Gyanvi Boutique Hub, Bettiah",
    deliveryDelayed: false,
    rtoFlagged: false,
    rtoReason: "",
    cancelReason: "",
    cancelledBy: "",
    cancellationDate: "",
    refundStatus: "Not Applicable",
    refundAmount: 0,
    stockRestored: false,
    holdReason: "",
    holdDate: "",
    holdNotes: "",
    holdExpiryAlert: "48 hrs Hold Limit",
    adminApprovedHold: true,
    sellerHoldPermission: true,
    orderHistoryLog: ["17 Nov: Order placed via UPI"],
    city: "Patna",
    state: "Bihar",
    pinCode: "800001",
    customProductImageUri: "",
    createdAt: past,
    updatedAt: past,
    ...overrides,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read, list, create, update, or delete orders", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("orders").get());
  await assertFails(
    unauthDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("orders")
      .doc("OD1001")
      .set(buildValidOrderPayload(ALICE_UID, "OD1001"))
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("orders").doc("OD1001").delete()
  );
});

test("Authenticated owner: valid Kotlin-mirrored order create, list, get, and partial updates succeed", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ordersCol = aliceDb.collection("users").doc(ALICE_UID).collection("orders");

  await assertSucceeds(ordersCol.doc("OD1001").set(buildValidOrderPayload(ALICE_UID, "OD1001")));
  await assertSucceeds(ordersCol.get());
  await assertSucceeds(ordersCol.doc("OD1001").get());

  // Partial update: Cancelled with Age/Years, AWB update, and Refund status
  await assertSucceeds(
    ordersCol.doc("OD1001").update({
      status: "CANCELLED",
      sellerStage: "Cancelled",
      cancelReason: "गलत Size या Colour चुना गया",
      cancelledBy: "Customer",
      cancellationDate: "17 Nov 2026",
      refundStatus: "Initiated",
      refundAmount: 599,
      stockRestored: true,
      customerAgeYears: "27 Years",
      awbNumber: "EKRT-99887766",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  await assertSucceeds(ordersCol.doc("OD1001").delete());
});

test("Cross-user isolation: Alice cannot read, list, create, update, or delete Bob's orders", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx
      .firestore()
      .collection("users")
      .doc(BOB_UID)
      .collection("orders")
      .doc("OD_BOB_1")
      .set(buildValidOrderPayload(BOB_UID, "OD_BOB_1"));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobOrders = aliceDb.collection("users").doc(BOB_UID).collection("orders");

  await assertFails(bobOrders.get());
  await assertFails(bobOrders.doc("OD_BOB_1").get());
  await assertFails(bobOrders.doc("OD_BOB_1").update({ customerName: "Hijacked", updatedAt: new Date(Date.now() - 500) }));
  await assertFails(bobOrders.doc("OD_BOB_2").set(buildValidOrderPayload(ALICE_UID, "OD_BOB_2")));
  await assertFails(bobOrders.doc("OD_BOB_1").delete());
});

test("Hardening Audit: rejects @DocumentId 'id' key, ghost shadow fields, future timestamps, and immutable mutations", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ordersCol = aliceDb.collection("users").doc(ALICE_UID).collection("orders");

  // 1. Reject extra @DocumentId `id` field inside document body
  await assertFails(
    ordersCol.doc("OD_GHOST_ID").set(buildValidOrderPayload(ALICE_UID, "OD_GHOST_ID", { id: "OD_GHOST_ID" }))
  );

  // 2. Reject future createdAt timestamp
  await assertFails(
    ordersCol
      .doc("OD_FUTURE")
      .set(buildValidOrderPayload(ALICE_UID, "OD_FUTURE", { createdAt: new Date(Date.now() + 120000) }))
  );

  // Create valid doc first
  await assertSucceeds(ordersCol.doc("OD2001").set(buildValidOrderPayload(ALICE_UID, "OD2001")));

  // 3. Shadow Update Test: ghost field `isVerified: true` must fail
  await assertFails(
    ordersCol.doc("OD2001").update({
      customerAgeYears: "28 Years",
      isVerified: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // 4. Immutable field mutation (createdAt / userId / orderId) must fail
  await assertFails(
    ordersCol.doc("OD2001").update({
      createdAt: new Date(Date.now() - 200),
      updatedAt: new Date(Date.now() - 100),
    })
  );
  await assertFails(
    ordersCol.doc("OD2001").update({
      userId: BOB_UID,
      updatedAt: new Date(Date.now() - 100),
    })
  );
});

test("Cancellation Policy & Email Spoofing Audit: blocks direct Shipped->Cancelled without RTO and rejects unverified admin email spoof", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const aliceDoc = aliceDb.collection("users").doc(ALICE_UID).collection("orders").doc("OD_SHIP_1");

  await assertSucceeds(
    aliceDoc.set(
      buildValidOrderPayload(ALICE_UID, "OD_SHIP_1", {
        status: "SHIPPED",
        sellerStage: "Shipped",
      })
    )
  );

  // Direct normal cancellation of shipped order without RTO flag must fail
  await assertFails(
    aliceDoc.update({
      status: "CANCELLED",
      sellerStage: "Cancelled",
      rtoFlagged: false,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // RTO-flagged cancellation of shipped order succeeds
  await assertSucceeds(
    aliceDoc.update({
      status: "CANCELLED",
      sellerStage: "Cancelled",
      rtoFlagged: true,
      rtoReason: "Customer rejected at doorstep (RTO)",
      updatedAt: new Date(Date.now() - 400),
    })
  );

  // Email spoofing test: matching admin email with email_verified: false must fail on Bob's order
  const spoofAdminDb = testEnv
    .authenticatedContext("spoof_uid", {
      email: "kumaramitbth4@gmail.com",
      email_verified: false,
    })
    .firestore();
  await assertFails(
    spoofAdminDb.collection("users").doc(ALICE_UID).collection("orders").doc("OD_SHIP_1").get()
  );

  // Verified admin email succeeds
  const verifiedAdminDb = testEnv
    .authenticatedContext(ADMIN_UID, {
      email: "kumaramitbth4@gmail.com",
      email_verified: true,
    })
    .firestore();
  await assertSucceeds(
    verifiedAdminDb.collection("users").doc(ALICE_UID).collection("orders").doc("OD_SHIP_1").get()
  );
});
