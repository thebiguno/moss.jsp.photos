function getWindowSize() {
	var width = 0;
	var height = 0;
	if( typeof( window.innerWidth ) == 'number' ) {
		//Non-IE
		width = window.innerWidth;
		height = window.innerHeight;
	}
	else if( document.documentElement && ( document.documentElement.clientWidth || document.documentElement.clientHeight ) ) {
		//IE 6+ in 'standards compliant mode'
		width = document.documentElement.clientWidth;
		height = document.documentElement.clientHeight;
	} 
	else if( document.body && ( document.body.clientWidth || document.body.clientHeight ) ) {
		//IE 4 compatible
		width = document.body.clientWidth;
		height = document.body.clientHeight;
	}
	
	return {width: width, height: height};
}

function Slideshow(image, sourceList, fadeSpeed, photoSpeed, center) {
	var listCounter = 0;
	var fadeSteps = 8.0; //How many steps to go through when fading
	var opacity = 0.0;
	var fadeIn = true; //What is the state?
	
	function nextImage(){
		if (opacity <= 0.0){
//			console.log("switch image");
			image.onload = function() {
				//Center image if desired
				if (center){
					var size = getWindowSize();
					image.style.left = ((size.width - image.naturalWidth) / 2) + 'px';
					image.style.top = ((size.height - image.naturalHeight - 100) / 2) + 'px';					
				}
				
				//Start fading in
				opacity = 1.0 / fadeSteps;
				fadeIn = true;
				window.setTimeout(function() {
					nextImage();
				}, 10);
			};
			image.src = sourceList[listCounter];

			listCounter++;
			if (listCounter >= sourceList.size())
				listCounter = 0;
		}
		//If we are supposed to be fading in...
		else if (fadeIn){
//			console.log("fade in");
//			console.log(opacity);
			//If we are still fading in
			if (opacity < 1.0){
				opacity += 1.0 / fadeSteps;
				image.style.opacity = opacity;
				window.setTimeout(function() {
					nextImage();
				}, fadeSpeed / fadeSteps);
			}
			else {
				//Schedule the next time we start to fade out
				fadeIn = false;
				window.setTimeout(function() {
					nextImage();
				}, photoSpeed);				
			}
		}
		else if (!fadeIn){
//			console.log("fade out");
//			console.log(opacity);
			
			//If we are still fading in
			if (opacity > 0){
				opacity = opacity - 1.0 / fadeSteps;
				image.style.opacity = opacity;
				window.setTimeout(function() {
					nextImage();
				}, fadeSpeed / fadeSteps);
			}
			else {
				//Schedule the next time we start to fade out
				fadeIn = false;
				window.setTimeout(function() {
					nextImage();
				}, photoSpeed);				
			}
		}
		
		
	}
	
	nextImage();
}