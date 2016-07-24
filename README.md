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
    * `:project` the project your are tracking time for (nettime code)
    * `:data-dir` a directory where app data is stored

    optional:
    * `:algo` one of:
      * `:accumulate`: aggregate all wake periods into a continuos entry
      * `:exact` track all individual sleep/wake periods as they happened
      * `:maximise` (default) start the entry with the first wake event of the day and end it with the last sleep event 
    * `:manual-collection` a boolean: disables the power management based data collection
    
## Manual Data Collection

If you prefer you can control the time data tracked and disable automatic collection based on your machines activity with the `manual-collection` config flag. In order to manually enter time worked you have to edit the `tail.edn` file in the data dir which contains the most recently collected data. The records have the form: 

```
{
  :time-tracker.store/status :collected
  :time-tracker.store/tz "Europe/Vienna"
  :time-tracker.store/from #inst "2016-06-12T09:34:52.000-00:00"
  :time-tracker.store/to #inst "2016-06-12T14:46:47.000-00:00"
}
```

The format is called [edn](https://github.com/edn-format/edn).

* Timezone information will be used when entering the tracked time into nettime and does not necessarily have to be the same as in `from` and `to`.
* The status value will change after tracking to either `:tracked` or `:error`

## License

Copyright © 2016 Peter Brachwitz

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
