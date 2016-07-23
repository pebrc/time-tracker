(ns time-tracker.log
  (import org.slf4j.bridge.SLF4JBridgeHandler))

(defn init []
  (SLF4JBridgeHandler/removeHandlersForRootLogger)
  (SLF4JBridgeHandler/install))
