# Blog posts App

This is a Jetpack Architecture based Android App of posting and reading other users blogs.
The App interacts with the website "open-api.xyz" (sandbox website for the App rest-api).

## Structure:

### Authorizatio section

* Login screen
* New registration screen
* Forgot Password (WebView fragment)
    
### Main section
    
* Posts feed screen
* Create new post screen (select photo from internal storage, crop and uplaod to the server)
* Account screen

## Main features of the App

1. Kotlin:

2. Coroutines:
    * Advanced coroutine management using jobs  
    * Cancelling active jobs
    * Coroutine scoping
    
3. Navigation Components:
    * Bottom Navigation View with fragments  
    * Leveraging multiple navigation graphs
    
4. Dagger 2:
    * custom scopes, fragment injection, activity injection, Viewmodel injection
    
5. MVI architecture:
    * Basically this is MVVM with some additions
    * State management  
    * Building a generic BaseViewModel 
    * Repository pattern (NetworkBoundResource)

6. Room Persistence:
    * SQLite on Android with Room Persistence library
    * Custom queries, inserts, deletes, updates
    * Foreign Key relationships
    * Multiple database tables

7. Cache:
    * Database caching (saving data from network into local cache)
    * Single source of truth principal

8. Retrofit 2:
    * Handling any type of response from server (success, error, none, etc...)
    * Returning LiveData from Retrofit calls (Retrofit Call Adapter)

9. ViewModels:
    * Sharing a ViewModel between several fragments
    * Building a powerful generic BaseViewModel

10. WebViews:
    * Interacting with the server through a webview (Javascript)

11. SearchView:
    * Programmatically implement a SearchView
    * Execute search queries to network and db cache

12. Images:
    * Selecting images from phone memory
    * Cropping images to a specific aspect ratio
    * Setting limitations on image size and aspect ratio
    * Uploading a cropped image to server

13. Network Request Management:
    * Cancelling pending network requests (Kotlin coroutines)
    * Testing for network delays

14. Pagination:
    * Paginating objects returned from server and database cache

15. Material Design:
    * Bottom Navigation View with Fragments
    * Customizing Bottom Navigation Icon behavior
    * Handling Different Screen Sizes (ConstraintLayout)
    * Material Dialogs
    * Fragment transition animations
