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

function SlideShow(slideel, fadingSpeed, stopTime) {
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
					fadeIn(40);
				}, fadingSpeed);
			}

			this.stop = function() {
				window.setTimeout(function() {
					fadeOut(40);
				}, fadingSpeed);
			}
			function fadeIn(i) {
				img.style.opacity = 1. - i/40.;
				if(--i >= 0) {
					window.setTimeout(function() {
						fadeIn(i);
					}, fadingSpeed);
				} else {
					nexttime= window.setTimeout(function() {
						self.next();
					}, 40 * fadingSpeed + stopTime);
				}
			}       
			function fadeOut(i) {
				outtime= null;
				img.style.opacity = i/40.;
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