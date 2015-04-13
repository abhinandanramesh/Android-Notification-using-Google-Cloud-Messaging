GCM-cloud-end-points-Android-app
===============

The backend server and the front end client side implementation to use Google Cloud Messaging Service for Android.

Refer to the documentation for Google Cloud Messaging Service at http://developer.android.com/google/gcm/index.html

Here, the backend server is hosted on Google App engine.

To set up a HTTP server on standard web server, refer to http://developer.android.com/google/gcm/http.html#auth

To create only the backend for an existing android app, refer to https://developers.google.com/eclipse/docs/endpoints-create-fromandroid

Tweak the allowed number of users parameter in the gcmdemoapp-AppEngine/src/com/gcmdemoapp/MessageEndPoint.java class. Currently, I have configured the allowed number of users to 5000.

Add annotations for the cloud end points as needed.

I have automated the process of GCM registration along with the Google sign in process. Add the corresponding Google Play Services library for the sign-in. Please use the library as I have uploaded for the java files to compile to compile without any overhead or additional dependencies. Add the necessary dependencies if the latest version of Google play services library is used.

The notification is built and handled using notification builder in the onmessage handler in GCMIntentService.java
