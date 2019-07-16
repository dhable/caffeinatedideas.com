# Caffeinated Ideas Static Site

Boot script to generate static HTML files from blog content.


## To Do List

There's a list of things that I need to work on when I get time to do so:

* Replace the boot-s3 upload functionality with code local to the project. The
lib has a bunch of spec violations that's preventing me from moving this repo to
the latest version of Clojure.

* Upgrade Clojure (see above)

* Link Linter: Checks all of the HTML links to ensure that they point to live sites and 
  pages still. Generates a report of all links that need to be addressed.

* More Unit Test Coverage

* Spell Checker

* Thumbnail Image Handling: Provide a new markdown handler to generate thumbnail images and 
  embed those on the main page but provide a link to an expanded version.
  
* Auto linker: Parse through a blog post and suggest things that should become links. Offer to
insert the links into the markdown.


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
