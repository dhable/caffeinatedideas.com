/**
 * Dynamic, client side implmentation of the Thoughtworks technology radar
 * concept using D3 and jQuery. Data is dynamically loaded from the server
 * via a JSON file.
 *
 * This function creates a self contained instance of the radar visualization
 * and returns an object which is an instance to the public interface for
 * the radar. Note that this control uses a functional object creation style
 * to avoid leaking instance variables in the global scope.
 *
 * @param selector The D3 selector for the DOM container for the visulaization.
 *
 * @param dataUrl The URL to load the JSON data file from. Loading is only 
 *  attempted when render() is called.
 *
 * @param configuration A JS object containing key/value options that allow you
 *  to customize the radar. Valid options include:
 *    controlSize:
 *    outlineColor:
 *    highlightColor:
 *    pointColor:
 *    pointHighlightColor:
 *    pointActiveColor:
 *
 * TODO:
 *    radar.js:
 *      - Fix point layouts!!!!!!!!!!!!!!!!!
 *      - Add text label to the layers
 */

function d3Radar(selector, dataUrl, configuration) {
  /////////////////////////////////////////////////////////////////////////////////////////////////
  // PRIVATE DATA AND INITIALIZATION
  /////////////////////////////////////////////////////////////////////////////////////////////////  
  var canvas,
      radarData,
      conf = configuration,
      callbacks = {"section:display": [], "section:clear": [], "section:open": [],
                   "section:close": [], "entry:display": [], "entry:clear": [],
                   "entry:open": [], "entry:close": []};

  if(!configuration.controlSize)
    throw new Error("d3Radar requires control size parameter.");
  conf.controlSize = configuration.controlSize;

  conf.fillColors = configuration.fillColors || ["#FFFFB2", "#FFCCCC", "#CCEBD6", "#CCCCFF"];
  if(!_.isArray(conf.fillColors) || conf.fillColors.length < 4)
    throw new Error("d3Radar fillColors needs to be an array with 4 colors defined.");

  conf.outlineColor = configuration.outlineColor || "black";
  conf.highlightColor = configuration.highlightColor || "red";
  conf.pointColor = configuration.pointColor || "black";
  conf.pointHighlightColor = configuration.pointHighlightColor || "red";
  conf.pointActiveColor = configuration.pointActiveColor || "blue";


  /////////////////////////////////////////////////////////////////////////////////////////////////
  // PRIVATE HELPER FUNCTIONS
  /////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Internal function for emitting events to any registered callback
   * functions on various user actions. The first parameter is the name
   * of the event to trigger. Subsequent parameters will be passed to
   * each event listener.
   */
  var emit = function() {
    var args = _.toArray(arguments),
        eventName = _.first(args),
        eventArgs = _.rest(args);

    _.each(callbacks[eventName], function(cb) { 
      cb.apply(null, eventArgs); 
    });
  };


  /**
   * Calculates an integer hash value from a string. Copied from
   * http://werxltd.com/wp/2010/05/13/javascript-implementation-of-javas-string-hashcode-method/
   */
  var hashCode = function(str) {
    var hash = 0;
    if (str.length == 0) return hash;
    for (i = 0; i < str.length; i++) {
      char = str.charCodeAt(i);
      hash = ((hash<<5)-hash)+char;
      hash = hash & hash; // Convert to 32bit integer
    }
    return hash;
  };


  /**
   * Determines where on the canvas the section's center point
   * will be located once the section is zoomed in to take
   * over the entire view. It returns a new [x,y] pair that 
   * contains the new center.
   * 
   * NOTE: This function only works when the visualization has
   *       four sections.
   */
  var zoomedSectionCenter = function(sectionId) {
    if(sectionId == 1) return [0,conf.controlSize];
    else if(sectionId == 2) return [0, 0];
    else if(sectionId == 3) return [conf.controlSize, 0];
    else return [conf.controlSize, conf.controlSize];
  };


  /**
   * Builds a D3 translate command based on an (x,y) pair. Used to
   * encapsulate the string manipulation throughout the visualization.
   */
  var d3Transform = function(point) {
    if(!_.isArray(point) && point.length != 2)
      throw new Error("d3Transform only designed for point objects as an array with two elements.");

    return "translate(" + point.join(",") + ")";
  };


  /**
   * This function is hacky. I would like to revisit positioning
   * the points using trig functions later on.
   */
  var d3TranslatePoint = function(quadrantId, point) {
    var origin = zoomedSectionCenter(quadrantId);
    // origin[0] = origin[0] + point[0];
    // origin[1] = origin[1] + point[1];

    if(quadrantId == 1) { // (0, 300)
      origin[0] = origin[0];
      origin[1] = origin[1] - point[1];
    } else if(quadrantId == 2) { 
      origin[0] = point[0];
      origin[1] = origin[1];    
    } else if(quadrantId == 3) {
      origin[0] = origin[0] - point[0];
      origin[1] = origin[1];
    } else if(quadrantId == 4) {
      origin[0] = origin[0] - point[0];
      origin[1] = origin[1] - point[1];
    }

    return origin;
  };


  /**
   * Extracted function for use with underscore's reduce method to find
   * the min and max hash code integers given an array of strings. This
   * is used in the D3 scale to help offset the points from the arc center
   * line. This makes the visualization seem more organic.
   */
  var findStrHashMinMax = function(current, point) {
    var str = point.name,
        hash = hashCode(str),
        result = current;

    switch(current.length) {
      case 0:
        result = [hash];
        break;

      case 1:
        result = current[0] < hash ? [current[0], hash] : [hash, current[0]];
        break;

      case 2:
        if(hash < current[0]) result = [hash, current[1]];
        else if(hash > current[1]) result = [current[0], hash];
        break;
    }

    return result;
  };


  /**
   * For a particular layer of a section, this function determines the best
   * way to layout the individual points on the radar using d3 symbols.
   * Behaviors are also attached to the given data points to support highlighting
   * and selecting the point for furter inspection.
   *
   * NOTE: This function assumes that there are four sections. It needs to
   *       be updated if the code needs to handle variable number of sections.
   */
  var placePoints = function(sectionId, innerRadius, outerRadius, pointData) {
    var angleStep = (Math.PI / 2) / (pointData.length + 1),
        hashRange = _.reduce(pointData, findStrHashMinMax, []),
        layerCenter = (innerRadius + outerRadius) / 2,
        radiusScale = d3.scale.linear()
                              .domain(hashRange)
                              .range([0.60 * (innerRadius - layerCenter) + layerCenter, 
                                      0.60 * (outerRadius - layerCenter) + layerCenter]);

    /**
     * Event handler tied to the mouse over event on a data point. This
     * visually highlights the point and emits an event to any listeners that
     * the point is of interest with a subset of the data for the point.
     */
    var onPointMouseOver = function(dataPoint) {
      var d3Point = d3.select(this);
      if(d3Point.attr("data-active") !== "true") {
        d3Point.style("fill", conf.pointHighlightColor);
        emit("entry:display", dataPoint.name, dataPoint.desc, dataPoint.analysis);
      }
    };

    /**
     * Event handler tied to the mouse out event on a data point. This removes
     * the visual highlighting and emits an event to any listeners that the point
     * is no longer of interest.
     */
    var onPointMouseOut = function() {
      var d3Point = d3.select(this);
      if(d3Point.attr("data-active") !== "true") {
        d3Point.style("fill", conf.pointColor);
        emit("entry:clear");
      }
    };

    /**
     * Event handler tied to the click event on a given data point. This will
     * toggle between opening and closing the point. Open points have a different
     * highlight that is cleared when the point is closed. Events are emitted with
     * the complete data set for a point.
     */
    var onPointMouseClick = function(dataPoint) {
      if(d3.select(this).attr("data-active") === "true") { // Deselect point
        d3.select(this).attr("data-active", null).style("fill", conf.pointHighlightColor);
        emit("entry:close");
      } else { // Select point
        d3.selectAll("[data-active='true']").attr("data-active", null).style("fill", conf.pointColor);
        d3.select(this).attr("data-active", true).style("fill", conf.pointActiveColor);
        emit("entry:open", dataPoint.name, dataPoint.desc, dataPoint.analysis);
      }
    };


    canvas.append("path")
          .style("fill", "red")
          .attr("transform", "translate(0,0)")
          .attr("d", d3.svg.symbol().type("square"));

    canvas.append("path")
          .style("fill", "red")
          .attr("transform", "translate(300,300)")
          .attr("d", d3.svg.symbol().type("square"));

    pointData.forEach(function(d, index) {
      var active = d.active || false,
          point = d3.svg.symbol().type(active ? "triangle-up" : "circle")
          r = radiusScale(hashCode(d.name)),
          quadrant = parseInt(sectionId),
          angle = ((quadrant - 1) * (Math.PI / 2) + (angleStep * (index + 1))),
          

          // adjustedAngle = angle,
          adjustedAngle = (quadrant % 2 == 0) ? angle + (Math.PI / 2)
                                              : angle - (Math.PI / 2),
          // pointXY = d3TranslatePoint(sectionId, [r * Math.sin(adjustedAngle), 
          //                                        r * Math.cos(adjustedAngle)]),


          origin = zoomedSectionCenter(sectionId),
          pointXY = [r * Math.sin(adjustedAngle), r * Math.cos(adjustedAngle)],

          mouseOverFn = _.partial(onPointMouseOver, d),
          clickFn = _.partial(onPointMouseClick, d);

      
      console.log("O = [%s, %s], P = [%s, %s]", origin[0], origin[1], pointXY[0], pointXY[1]);

      canvas.append("path")
            .style("fill", conf.pointColor)
            .attr("transform",  d3Transform(pointXY))
            .attr("d", point)
            .on("mouseover", _.debounce(mouseOverFn))
            .on("mouseout", _.debounce(onPointMouseOut))
            .on("click", _.debounce(clickFn));
    });
  };


  /**
   * Given the radar data for a given section, information about the section
   * and the canvas, this function draws a zoomed in view of the section. In
   * the zoomed in view, each layer is seperate and the individual points are
   * rendered for display. Behaviors related to selecting an individual point
   * are also enabled.
   *
   * NOTE: This function assumes that there are four sections. It needs to
   *       be updated if the code needs to handle variable number of sections.
   */
  var drawZoomedSection = function(sectionId, size, name, color, layerData) {
    var fillColor = d3.rgb(color),
        sectionCenter = zoomedSectionCenter(sectionId),
        totalArea = ((Math.PI * Math.pow(size, 2)) / 4),        
        layerArea = totalArea / layerData.length + 1,
        startAngle = (sectionId - 1) * (Math.PI / 2),
        endAngle = sectionId * (Math.PI / 2),
        innerRadius = 0,
        outerRadius = 0;

    layerData.forEach(function(d, layerId) {
      outerRadius =  Math.sqrt((4 * (layerArea * (layerId + 1))) / Math.PI);

      var sectionLayer = d3.svg.arc()
                               .innerRadius(innerRadius)
                               .outerRadius(outerRadius)
                               .startAngle(startAngle)
                               .endAngle(endAngle);

      canvas.append("path")
            .style("stroke", "black")
            .style("fill", fillColor)
            .style("opacity", 0.25)
            .attr("d", sectionLayer)
            .attr("transform", d3Transform(sectionCenter));

      placePoints(sectionId, innerRadius, outerRadius, d.data);

      innerRadius = outerRadius;
    });

    emit("section:open", name, color);
  };


  /**
   * Given the complete radar data set, a canvas and the size of the
   * canvas, this function draws all of the sections in the radar and
   * and interactive behaviors to enable zooming into a specific section
   * of the radar.
   */
  var drawRadar = function() {
      var radarSize = conf.controlSize,
          radarOrginX = radarSize / 2,
          radarOrginY = radarSize / 2,
          sectionRadius = radarSize / 2;

      /**
       * Event handler tied to the mouse over event on a specific
       * section. This highlights that section in the view and triggers
       * a radar event to notify any listeners that the section is 
       * currently of interest to the user.
       */
      var onSectionMouseOver = function(title, desc, color) {
        d3.select(this)
          .style("opacity", 1)
          .style("stroke", conf.highlightColor)
          .style("stroke-width", 2);
        emit("section:display", title, desc, color);
      };

      /**
       * Event handler tied to the mouse out event on a specific section.
       * This removes any highlighting from the view and triggers a radar
       * event to notify any listeners that the section is no longer
       * highlighted.
       */
      var onSectionMouseOut = function() {
        d3.select(this)
          .style("opacity", 0.25)
          .style("stroke", conf.outlineColor)
          .style("stroke-width", 1);
        emit("section:clear");
      };

      /**
       * Event handler tied to the click event on a specific section of the
       * radar. This kicks off the transition to zooming into the section
       * in more details.
       */
      var onSectionClick = function(name, color, data) {
        var clickedSection = d3.select(this).attr("data-section"), // still in mouse handler
            otherSections = _(["1", "2", "3", "4"]).without(clickedSection), // TODO: This would need to change to support more sections.
            otherSectionSelector = _.map(otherSections, function(id) { return "[data-section='" + id + "']"}).join(","),
            zoomedCenterCoord = zoomedSectionCenter(clickedSection);

        d3.selectAll(otherSectionSelector)
            .transition()
            .duration(500)
            .delay(250)
            .style("opacity", 0)
            .remove();

        d3.select(this).transition()
            .duration(750)
            .delay(250)          
            .attr("transform", d3Transform(zoomedCenterCoord))
            .each("end", _.partial(drawZoomedSection, clickedSection, radarSize, name, color, data))
            .remove()
            .ease();
      };

      radarData.forEach(function(sectionData, sectionIndex) {
        var name = sectionData.name,
            desc = sectionData.desc,
            data = sectionData.layers,
            section = d3.svg.arc()
                            .innerRadius(0)
                            .outerRadius(sectionRadius)
                            .startAngle(sectionIndex * (Math.PI / 2))
                            .endAngle((sectionIndex + 1) * (Math.PI / 2)),
            mouseOverFn = _.partial(onSectionMouseOver, name, desc, conf.fillColors[sectionIndex]),
            clickFn = _.partial(onSectionClick, name, conf.fillColors[sectionIndex], data);

        canvas.append("path")              
              .style("fill", conf.fillColors[sectionIndex])
              .style("stroke", conf.outlineColor)
              .style("opacity", 0)
              .attr("data-section", sectionIndex + 1)
              .attr("d", section)
              .attr("transform", d3Transform([radarOrginX, radarOrginY]))
              .on("mouseover", _.debounce(mouseOverFn))
              .on("mouseout", _.debounce(onSectionMouseOut))
              .on("click", _.debounce(clickFn));
      });

      d3.selectAll("svg > *")
        .transition()
        .duration(500)
        .delay(250)
        .style("opacity", 0.25)
        .ease();
  };


  /////////////////////////////////////////////////////////////////////////////////////////////////
  // PUBLIC OBJECT / PUBLIC METHODS
  /////////////////////////////////////////////////////////////////////////////////////////////////   
  return {
    /**
     * Kicks off generating the radar control. This includes fetching the data file,
     * generating the visualization using D3 and placing in into the DOM.
     */
    render: function() {
      d3.json(dataUrl, function(err, data) {
        radarData = data;
        canvas = d3.select(selector)
                   .append("svg")
                   .attr("width", conf.controlSize)
                   .attr("height", conf.controlSize);

        drawRadar();
      });
      return this;
    },


    /**
     * Register a event handler function to listen for events on specific radar events.
     */
    on: function(eventName, eventHandler) {
      if(_.has(callbacks, eventName))
        callbacks[eventName].push(eventHandler);
      return this;
    },


    /**
     * Wipes out the current state of the radar and then transitions back to
     * the opening view. This method will also emit events when closing
     * out the view.
     */
    returnToOverview: function() {
      if(!radarData || !canvas)
        throw new Error("render() needs to be called before manipulating the radar.");

      emit("entry:close");

      var debouncedDrawRadar = _.debounce(function() {
        drawRadar();
        emit("section:close");  
      }, 100);

      d3.selectAll("svg > *").transition()
                             .duration(500)
                             .delay(250)
                             .style("opacity", 0)
                             .each("end", debouncedDrawRadar)
                             .remove();
      
      return this;
    }
  };
}