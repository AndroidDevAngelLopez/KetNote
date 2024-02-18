# KetNote
<img src='./newfirst.png' width=100%>


It is a simple Todo-App where you can save, edit and delete your favorite notes. In addition to being able to add an image if you wish.

App details:

- Use the Google Auth API to log into Mongo and Firebase. 
- Saves the notes in Mongo Realm which allows us real-time communication to recover changes in the non-relational database as well as save locally on the device while the images are saved in Firebase Storage.
- *Now implements the concept of stories wich consist in a note with only title and image.
- *Now implements Gemini wich you can use to generate titles or text based on the image or just generate text from text. 
- you can share your notes with instagram.
- the app uses the latest jetpack component libraries such as Room,Hilt,Material 3, Navigation component, View Binding,
- uses third-party libraries and apis such as Glide,Mongo Realm,Firebase(auth,storage,crashlytics)
- Now you can share your stories or notes with instagram!

<img src='./newsecond.png' width=100%>

Goals:

With this app written in the old views and xml system, I would like to be able to demonstrate the use of clean architecture and the MVVM+ presentation pattern which consists of adding MVI elements as classes that save state to the view model Likewise, the skill in using the old views system to create a modern application using views and xml.

App Status (18/02/23) (2402 RC-2 TRINITY):

| action                                       | status        |  
|----------------------------------------------|---------------|
| add corrective and stability improvements    | in fast-track |  
| modularize app                               | in progress   |  
