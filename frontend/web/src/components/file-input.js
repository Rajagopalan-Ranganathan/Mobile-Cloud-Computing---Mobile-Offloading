import React, {Component} from "react";

export default class FileInput extends Component {

	getPictures(event) {
		var pictures = [];
		var files = event.target.files;
		var pending = files.length;
		for (var i = 0; i < files.length; i++) {
			var file = files[i];
			var reader = new FileReader();

			reader.onload = function (e) {
				var img = document.createElement("img");
				img.src = e.target.result;
				var scaledPic = this.preSizeImage(img);
				pictures.push(scaledPic);
				pending--;
				if (pending == 0) {
					this.props.getPictures(pictures);
				}
			}.bind(this);
			reader.readAsDataURL(file);
		}
	}

	preSizeImage(img) {
		var canvas = document.createElement('canvas');
		var ctx = canvas.getContext("2d");
		ctx.drawImage(img, 0, 0);

		var MAX_WIDTH = 600;
		var MAX_HEIGHT = 800;
		var width = img.width;
		var height = img.height;

		if (width > height) {
		  if (width > MAX_WIDTH) {
		    height *= MAX_WIDTH / width;
		    width = MAX_WIDTH;
		  }
		} else {
		  if (height > MAX_HEIGHT) {
		    width *= MAX_HEIGHT / height;
		    height = MAX_HEIGHT;
		  }
		}
		canvas.width = width;
		canvas.height = height;
		var ctx = canvas.getContext("2d");
		ctx.drawImage(img, 0, 0, width, height);

		return canvas.toDataURL("image/png");
	}

	render() {
		if (this.props.type == "camera") {
			return (
			  <form action="#" className="right">
			    <div className="file-field input-field">
			      <div className="btn">
			        <span><i className="material-icons">&#xE3B0;</i></span>
			        <input type="file" accept="image/*" capture="camera" onChange={this.getPictures.bind(this)}/>
			      </div>
			    </div>
			  </form>
		  	)
		} else if (this.props.type == "file") {
			return (
			  <form action="#" className="left">
			    <div className="file-field input-field">
			      <div className="btn">
			        <span>File</span>
			        <input type="file" multiple onChange={this.getPictures.bind(this)}/>
			      </div>
			    </div>
			  </form>
		  	)
		} else {
			return (
				<div></div>
			)
		}
	}
}