# 💵 GoDutch 💵

*Your first-choice app for **going dutch***

This project is for the 2nd week of *KAIST CS496 Immersion Camp: Intensive Programming and Startup*, the base requirements being **developing an android application using a backend server**.

## Login / Registration
|Launch Screen|Login|
|-------------|-----|
|<img src="https://i.imgur.com/g5NQBtD.jpg" width="400">|<img src="https://i.imgur.com/7Tnjumx.jpg" width="400">|

Login is implemented by using the Facebook SDK for Android. Upon the push of a button, the application will prompt users to register by putting in the phone number and bank account, both of which are used throughout the application.

## Tabs
This application consists of 3 *tabs* - *contacts*, *gallery*, and *godutch*

### Contacts
The central list shows a list of phone numbers stored in the DB. If the server connection is unstable, it shows only the contacts in the internal storage. In the search bar at the top, you can find your phone number by name. Press the floating action button at the bottom to upload the information from the internal storage to the DB. All the files related to the phone book are named home, so please refer to them when you modify them.

### Gallery
|Gallery Page|Deletion|
|------------|--------|
|<img src="https://i.imgur.com/gLxaigl.jpg" width="400>|<img src="https://i.imgur.com/f2dZAXc.jpg" width="400">|

The gallery tab shows the photos that the user has uploaded to his/her account. The user has 2 methods of uploading images to the server: from the gallery or directly from the photo taken from a camera.

The uploading process is done by sending a `multipart/form-data` content-type containing the images to the server. The server stores the images in its disk and serves the images using `nginx`, where the client retrieves the photos from.

Users can also delete photos by initiating a long press, allowing them to select the photos they wish to delete.

### GoDutch
The GoDutch tab is the main tab of this project. It helps a group of users [*go dutch*](https://en.wikipedia.org/wiki/Going_Dutch), which means evenly paying the total amount of money the group has spent.

This tab efficiently utilizes the [mongodb aggregation framework](https://docs.mongodb.com/manual/aggregation/) to process the complex relationships between various collections such as `transactions`, `parties`, and `users`.

#### Main Page
<img src="https://i.imgur.com/TPyNtFG.jpg" width="400">
The user is greeted by a main page of the tab, where the user can **create a new party** or **pay your debts**.

#### Creating a Party
<img src="https://i.imgur.com/qncbJV7.jpg" width="400" >
A **party** is a basically a group of people. One representative person paying for something is called a **transaction**. Transactions can occur between members in a group. **Resolving (정산)** transactions in a group calculates the most efficient money transferring relations and shows the results in a separate page. This page prompts the user to select members among *the registered users in his/her contact* to form a new group.

#### Party List
<img src="https://i.imgur.com/7zx8Wy0.jpg" width="400">
The following page shows the list of parties you are included in. Clicking a party takes you to the party details.

#### Party details
<img src="https://i.imgur.com/bQHeOOq.jpg" width="400">
The following page shows the information of the party. The members inlcuded in the party are shown on the top, and the list of transactions are shown below. Clicking `정산하기` shows the amount of money and the receiver that each member has to send to.

#### Pile of Debts (빚더미)
<img src="https://i.imgur.com/F1H8qIT.jpg" width="400">
The `Pile of Debts` section of the godutch main page shows the amount of money and the receiver that the user has to send to. Clicking on `청산` sends the user to the Toss application. When the user successfully sends money to the receiver, the pile of debts and the `정산` page will recognize this further on and not show your debts.

## Contributors
* [Paco Kwon](http://github.com/pacokwon)
* [Gyeongyeon Kim](http://github.com/KimGyeongyeon)

