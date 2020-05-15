(ns danhable.berg.commonmark
  (:require [clojure.string :as string])
  (:import [java.util ArrayList Collections]
           [org.commonmark.ext.gfm.tables TablesExtension]
           [org.commonmark.ext.heading.anchor HeadingAnchorExtension]
           [org.commonmark.ext.ins InsExtension]
           [org.commonmark.ext.gfm.strikethrough StrikethroughExtension]
           [org.commonmark.ext.autolink AutolinkExtension]
           [org.commonmark.parser Parser]
           [org.commonmark.renderer.html HtmlRenderer HtmlNodeRendererFactory HtmlNodeRendererContext HtmlWriter]
           [org.commonmark.node FencedCodeBlock]
           [org.commonmark.renderer NodeRenderer]))

(def enabled-extensions (ArrayList. [(TablesExtension/create)
                                     (AutolinkExtension/create)
                                     (StrikethroughExtension/create)
                                     (HeadingAnchorExtension/create)
                                     (InsExtension/create)]))

(def parser (-> (Parser/builder)
                (.extensions enabled-extensions)
                (.build)))

(defn language-class-name [node]
  (let [info (string/trim (.getInfo node))]
    (if (string/blank? info)
      "language-none"
      (str "language-" info))))

(defn line-numbers?
  [node]
  (not= (language-class-name node) "language-none"))

(defn code-renderer
  [^HtmlNodeRendererContext context]
  (let [^HtmlWriter html (.getWriter context)]
    (reify NodeRenderer
      (getNodeTypes [_]
        (Collections/singleton FencedCodeBlock))
      (render [_ node]
        (doto html
          (.line)
          (.tag "pre" (if (line-numbers? node)
                        {"class" "line-numbers"}
                        {}))
          (.tag "code" {"class" (language-class-name node)})
          (.text (.getLiteral node))
          (.tag "/code")
          (.tag "/pre")
          (.line))))))

(defn render-factory
  [renderer]
  (reify HtmlNodeRendererFactory
    (create [_ context]
      (renderer context))))

(def html-renderer (-> (HtmlRenderer/builder)
                       (.extensions enabled-extensions)
                       (.nodeRendererFactory (render-factory code-renderer))
                       (.build)))

(defn md-to-html-string [md]
  (->> md
       (.parse parser)
       (.render html-renderer)))