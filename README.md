### Problem Statement 
Develop a pseudo Mobile application (Android) which should list and open best out linked news articles from the app.

Check out the wireframe in [Adobe XD](http://sdev.in/inshorts)

### How to run 
  1. Source file added in `src` 
  2. Get tracking firebase configuration file from this url `https://firebase.google.com/` 
  3. Import/Open the project using Android Studio 
  4. Run the App

### Implemented feature 
  1. Used API to fetch news feed 
  2. Implemented paging to display the results (Shared will collect all the at once) by changing the parameter (page & limit) can get the data dynamically 
  3. Browser window added to display the news article 
  4. Added offline feature to browse the article headlines 
  5. Feature to filter by category added 

### Placeholder where we can extend the future In NewsProvider 
  1. To filter by category and publisher - By introducing URI `feed/<category>/<publisher>` 
  2. For Bookmark, By adding query (`is_bookmarked=true`) 
  3. By passing the orderby in cursor can do sorting (News Fragment)
  

Challenge Link [Inshorts Android App Development Hiring Challenge](https://www.hackerearth.com/challenge/hiring/inshorts-android-app-development-hiring-challenge/)

### Happy Coding
### 😃 
[sdev.in](https://www.sdev.in)
