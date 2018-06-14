'use strict'

// Require the Firebase package
const functions = require('firebase-functions');

// Initialize the account
const admin = require('firebase-admin');

// Initialize the sdk
admin.initializeApp(functions.config().firebase);


// Send notification function
exports.sendNotification = functions.database.ref('/Notifications/{user_id}/{notification_id}').onWrite((change, context)  => {

		// Obtain the destination user
		const user_id = context.params.user_id;
		const notification_id = context.params.notification_id;
		console.log('Destination User ID = ', user_id);

		// Obtain the source user 
		const sourceUser = admin.database().ref(`/Notifications/${user_id}/${notification_id}`).once('value');
		return sourceUser.then(sourceUserResult => {

			const from_user_id = sourceUserResult.val().from;
			console.log('Sender User ID = ', from_user_id);

			// Obtain source user information from database
			const userQuery = admin.database().ref(`/Users/${from_user_id}/fullname`).once('value');
			return userQuery.then(userDataResult => {
				const fullname = userDataResult.val();


				// Obtain the device token id from the database
				const deviceToken = admin.database().ref(`/Users/${user_id}/tokenId`).once('value');
				return deviceToken.then(result => {

					// Store token id
					const deviceTokenId = result.val();

					// Setup message payload
					const payload = {
						notification: {
							title: "New Friend Request",
							body: `${fullname} has sent you a friend request`,
							icon: "default",
							click_action : "com.scarlat.marius.chatapp_TARGET_NOTIFICATION"
						},
						data: {
							sender_id: from_user_id
						}
					};

					return admin.messaging().sendToDevice(deviceTokenId, payload).then(response => {
						return console.log('Notification has been sent to the user');
					});

				});

			});

		});
		
	});
