/**
 * Dynamic, client side implmentation of the Thoughtworks technology radar
 * concept using D3 and jQuery. Data is dynamically loaded from the server
 * via a JSON file.
 */

 $.ajax("/radardata/current.json")
  .done(function(data) {
    // TODO: plop the data structure somewhere
    console.log(data);
    drawRadarOverview();
 });

var drawRadarOverview = function() {
    var canvas = d3.select("#quad-view")
                   .append("svg")
                   .attr("width", 100)
                   .attr("height", 100);

    // canvas.append("circle")
    //       .style("stroke", "black")
    //       .style("fill", "red")
    //       .attr("r", 40)
    //       .attr("cx", 50)
    //       .attr("cy", 50);

    var q1 = canvas.append("arc")
                .style("stroke", "black")
                .style("fill", "red")
                .attr("innerRadius", 40)
                .attr("outerRadius", 100)
                .attr("startAngle", 0)
                .attr("endAngle", 3.14);

};