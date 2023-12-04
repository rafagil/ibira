# Ibira

This is a templating/state management library meant to simplify front-end development based on HTMx. Its main goal it's to be a quick way to develop web applications without much boilerplate and server configuration. It should also stay out of your way when you need to do things differently.

## How does the template looks like:
Ibira was created with the idea of using functions (macros) to represent the html document structure:
```clojure
(html
  (head
    (title "Hello World"))
    (body {:class "body"}
          (h1 "Hello World")))	
	
```
It also have some helpers to handle state changes and updates (inspired in Redux):
```clojure
(body
  (watch [counter :counter]
    (span (str "The counter is: " counter)))
  (action button {:hx-trigger "click" :type "button"} {:type "INCREMENT"} "Increment"))
```
In the example above, the `watch` macro will re-render its contents whenever the `:counter` state changes, and the `action` macro will just dispatch an action with the type "INCREMENT". The request will be made using HTMx, and the Ibira ring middleware will intercept the calls and generate proper headers to update the view.
Since everything is build inside functions, any clojure code can be used and works as expected: 
```clojure
(watch [item :items]
  (ul
    (map #(li %) item)))
```
	
More Examples of usage can be found in the "examples" directory.

## What types of app can be done with Ibira?
Any web app can use Ibira (even non web apps that need to render html for any reason), but the main things I believe it's going to help a lot are:
* Front-end for IoT projects
* Dashboards/Monitoring Apps (single state across multiple clients)
* Electron Apps
* Offline PWAs (the goal is to support WebAssembly in the future, maybe using ClojureDart)

## Features
* The template "functions" (html, head, etc..) are in fact macros with the purpose of doing the "parsing" during compile time, avoiding unecessary checks during runtime. At runtime, they're just functions that concatenate strings to build the template.
* Can be used with ClojureScript and run completely in the browser (there is a WIP example called "shopping-list" that uses a ServiceWorker to achieve that)
* Supports multple stores, so you can use mutiple strategies for persisting the state (memory, database, session, cache, etc.)
* It's just Clojure functions! So, feel free to use map, reduce, if, condp etc. inside your teplates. They just work out of the box!


## Current State:
This library is still in its very early stages of development. It's very usable at the moment, but expect breaking changes.
The basic functionality works and I'm using on some of my personal projects, but still there are a few improvements that need to be made:

## Future
* Improve documentation!
* Add automated tests
* Avoid multiple requests to update different elements on the page
* Fix issues with map on ClojureScript
* General improvements based on usage.
* Html Escaping support.
* Maybe improve the `watch` macro to make it more consistent with the `action` macro and allow for multiple children
* Maybe create a small tool to bootstrap projects.
* Add mandatory animated gifs!
