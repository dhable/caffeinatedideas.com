# Caffeinated Ideas Static Site

Boot script to generate static HTML files from blog content.


## Roadmap

This is a rough list of improvements I'd like to make to the site generator:

* Draft Mode
  A way of keeping all in progress posts in the source tree but excluded from the publish
  task.
  
* Author UI
  Build a simple ClojureScript UI that provides a means of editing the content and post
  metadata files for me. Single click to move from draft to published file.
   
* Link Linter
  Checks all of the HTML links to ensure that they point to live sites and pages still.
  Generates a report of all links that need to be addressed.
  
* More Unit Test Coverage

* Setup CI Builds
  Setup a free tier CI build on the repo to avoid having broken unit tests sit too long
  in the code base.
  
* Spell Checker

* Thumbnail Image Handling
  Provide a new markdown handler to generate thumbnail images and embed those on the main
  page but provide a link to an expanded version.


## Licenses

### Clojure Source Code

   Copyright 2017 Daniel T. Hable.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

### Site Content

   This work by Daniel T. Hable is licensed under a Creative Commons Attribution-ShareAlike 3.0 Unported License.