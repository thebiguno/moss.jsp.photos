/*
 * Script to fade through a list of images in slideshow fashion.  Modified from
 * code at http://www.bytemycode.com/snippets/snippet/762/ (improved by Jeff in
 * same forum's comments; further modified by Wyatt).
 */
Object.prototype.addEvent = function(evtType, func) {
	if (this.addEventListener) {
		this.addEventListener(evtType, func, true);
	} else if (this.attachEvent) {
		this.attachEvent('on' + evtType, func);
	} else {
		this['on' + evtType] = func;
	}
}

function startSlideshow(slideel, sourceList, fadeSpeed, photoSpeed, center) {
	for (var i = 0; i < sourceList.length; i++){
		var img = document.createElement("img");
		img.setAttribute("style", "opacity: 0; filter:alpha(opacity=0); position: absolute;");
		img.setAttribute("src", sourceList[i]);
		slideel.appendChild(img);
	}
	
	//Center the loading image if we have been asked for it... we have to do it twice to allow
	// for the fact that it may or may not be in cache - if it is in cache, then the first 
	// time it will work (as the width and height are already defined); otherwise, we will
	// have to schedule the onload event.
	//If the image is in cache already, it will not fire an onload event on some browsers.
	if (center){
		var slideshowLoadingImage = document.getElementById('slideshowLoadingImage');
		slideshowLoadingImage.style.left = ((size.width - slideshowLoadingImage.naturalWidth) / 2) + 'px'; 
		slideshowLoadingImage.style.top = ((size.height - slideshowLoadingImage.naturalHeight) / 2) + 'px';
		slideshowLoadingImage.onload = function() {
			var slideshowLoadingImage = document.getElementById('slideshowLoadingImage');
			slideshowLoadingImage.style.left = ((size.width - slideshowLoadingImage.naturalWidth) / 2) + 'px'; 
			slideshowLoadingImage.style.top = ((size.height - slideshowLoadingImage.naturalHeight) / 2) + 'px';
	
		}
	}
	
	new SlideShow(slideel, fadeSpeed, photoSpeed, center);
}

function SlideShow(slideel, fadingSpeed, stopTime, center) {
	var nexttime= null;

	this.next = function() {
		nexttime= null;
		this.current.stop();
		this.current = this.current.nextSlide;
		this.current.start();
	}

	function createSlides() {
		var imgs = slideel.getElementsByTagName('img');
		var slides = [];

		for (var i = 0; i < imgs.length; i++) { 
			slides[i] = new Slide(imgs[i], self);
		}

		for (var i = 0; i < slides.length; i++) {
			if (i == slides.length - 1)
				slides[i].nextSlide = slides[0];
			else
				slides[i].nextSlide = slides[i + 1];
		}

		self.current =  slides[0];
		slides[0].start();

		function Slide(img, slideShow) {
			img.style.opacity = '0';

			this.start = function() {
				window.setTimeout(function() {
					fadeIn(10);
				}, fadingSpeed);
			}

			this.stop = function() {
				window.setTimeout(function() {
					fadeOut(10);
				}, fadingSpeed);
			}
			function fadeIn(i) {
				var size = getWindowSize();
				if (img.naturalWidth > 0 && img.naturalHeight > 0){
					img.style.left = ((size.width - img.naturalWidth) / 2) + 'px'; 
					img.style.top = ((size.height - img.naturalHeight) / 2) + 'px';
					img.style.opacity = 1. - i/10.;
					document.getElementById('slideshowLoadingImage').style.opacity = 0;
//					document.getElementById('slideshowLoadingImage').style.visibility = 'hidden';
				}
				else {
					img.style.opacity = 0;
				}
				if(--i >= 0) {
					window.setTimeout(function() {
						fadeIn(i);
					}, fadingSpeed);
				} else {
					nexttime= window.setTimeout(function() {
						self.next();
					}, 20 * fadingSpeed + stopTime);
				}
				
			}       
			function fadeOut(i) {
				outtime= null;
				if (img.naturalWidth > 0 && img.naturalHeight > 0){
//					img.style.left = ((size.width - img.naturalWidth) / 2) + 'px'; 
//					img.style.top = ((size.height - img.naturalHeight) / 2) + 'px';
					img.style.opacity = i/10.;
				}
				else {
					img.style.opacity = 0;
				}
				if(--i >= 0) {
					window.setTimeout(function() {
						fadeOut(i);
					}, fadingSpeed);
				}
			}       
		}
	}

	var self = this;
	createSlides(slideel);
}

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