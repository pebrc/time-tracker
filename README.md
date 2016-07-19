# time-tracker

Tracks time worked based on you machine's power management log.

## Installation

* Binaries currently not available
* Requires PhantomJs 2.1 on the PATH (tested with 2.1.1)


## Build
    lein uberjar

## Usage

    

    $ java -jar time-tracker-0.1.0-standalone.jar [args]

## Options

* `-c config` a edn config file containing
    the keys
    * `:user` nettime user name
    * `:pw` nettime password
    * `:project` the project your are tracking time for

    optional:
    * `:algo` one of:
      * `:accumulate`: aggregate all wake periods into a continuos entry
      * `:exact` track all individual sleep/wake periods as they happened
      * `:maximise` (default) start the entry with the first wake event of the day and end it with the last sleep event 


## License

Copyright © 2016 Peter Brachwitz

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
