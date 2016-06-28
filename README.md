# nomenclature

This is a teeny little project built to answer one question: **What do people name their Java classes?**

>There are only two hard things in computer science: cache invalidation and naming things.

We've all been there: You've created some component that serves some sort of function in your application, but what do 
you call it?  You want your code to be readable and maintainable, not just by others but by future you.  When someone 
looks at your code, months or years from now, what name immediately evokes the right image in their brain?  The name
that makes them go, "Ah yes, this is a Schmargler class, I have a good idea of what this does now!"

To get us one step closer to the answer, this project grabs all the GitHub repositories, labeled as Java projects, with
more than 5k stars (as a very imperfect proxy for repo popularity).  It takes all the Java class names of the form
(prefix)(root) and counts how many times each root appears.  (Classes ending in "Test" or "Impl" are ignored to avoid
double-counting.)

Here's the result from running on June 28, 2016, with some commentary:

    Count   Root
    ----------------------------------
       589  Exception    # good to see that people are using Exceptions...
       546  Factory      # aw yeah, design patterns!
       535  Handler
       436  Builder
       395  Action
       365  Activity
       356  Utils        # sometimes there's just no better place to put these functions, eh?
       347  IT
       304  Configuration
       267  Request
       234  Case
       228  Listener
       225  Service      # I thought this would be higher, as a sort of catch-all...
       222  Generated
       221  Bean
       216  Resolver
       214  Adapter
       213  Context
       212  Provider
       209  Data
       204  2            # we must have a lot of 2nd versions of things
       203  Parser
       184  Type
       181  View
       176  Response
       174  Manager
       171  Util
       166  Tester
       152  Application
       148  Source
       145  Map
       144  Decoder
       142  Converter
       140  Loader
       138  Helper
       136  Processor
       132  Config       # probably a lot of Spring repos out there
       129  Fragment
       126  Generator
       125  Stream
       121  Properties
       118  Filter
       109  Encoder
       108  3
       103  Interceptor
       101  Info
       101  Cache
       100  Module      # probably a lot of Guice repos out there

----

This tiny one-off app is built in Java 8 and Spring Boot, and leverages
the [Eclipse EGit GitHub Connector](https://github.com/eclipse/egit-github).  

Feel free to clone this repo and play with it.  Make it more resilient, or make it use GitHub credentials to get around
GitHub API's rate limiting.  Choose the distribution of repos by number of contributors instead of stars.  Put in some
more advanced logic for picking roots out of class names, or put some swanky visualization on top of it.

