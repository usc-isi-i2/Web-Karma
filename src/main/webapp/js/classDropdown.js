
$(function() {
            	  
  var $classDropdownMenu = $("#classDropdownMenu");
  var $rowClicked;
  
  
  $("body").on("click", "div svg g.InternalNode", function(e) {
	$rowClicked = $(this);  
	$classDropdownMenu.css({
      display: "block",
      left: e.pageX,
      top: e.pageY
    });
    return false;
  });
  
  $classDropdownMenu.on("click", "a", function () {
	    var message = "You clicked on the row '" + 
	    $rowClicked.children("*")[1].innerHTML + "'\n";
	    message += "And selected the menu item '" + $(this).text() + "'"
	    alert(message);
	    $classDropdownMenu.hide();
  });
  
  $(document).click(function () {
	  $classDropdownMenu.hide();
	});
  
});
