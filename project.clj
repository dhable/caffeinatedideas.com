(defproject cryogen "0.1.0"
            :description "Source code for my personal blog site caffeinatedideas.com"
            :url "https://github.com/dhable/caffeinatedideas.com"
            :dependencies [[org.clojure/clojure "1.7.0"]
                           [ring/ring-devel "1.4.0"]
                           [compojure "1.4.0"]
                           [ring-server "0.4.0"]
                           [cryogen-markdown "0.1.4"]
                           [cryogen-core "0.1.41"]]
            :plugins [[lein-ring "0.9.7"]]
            :main cryogen.core
            :ring {:init cryogen.server/init
                   :handler cryogen.server/handler})
