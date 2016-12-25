import React, {Component} from 'react';
import ReactDOM from 'react-dom';
import Login from "./components/login";
import Dashboard from "./components/dashboard";
import HistoryItem from "./components/history-item";
import OcrResult from "./components/ocr-result";
import Benchmark from "./components/benchmark";
import Tesseract from 'tesseract.js'
import "./styles/styles.scss";
import config from "./config";

class App extends Component {

	constructor(props) {
		super(props);
		var logged = (localStorage.bearer_token ? true : false)
		this.state = {logged: logged, ocrItem: null, ocrPictures: [], mode: "remote", online: navigator.onLine};
	}

	componentWillMount() {
		this.checkOnline();
		if (this.state.online && !localStorage.offlineImg) {
			var offlineImgSrc = require('./imgs/no_img.jpg');
			var offlineImg = new Image();
			offlineImg.onload = function (e) {
				localStorage.setItem("offlineImg", this.getBase64Image(offlineImg));
				Tesseract.recognize(offlineImg);
			}.bind(this);
			offlineImg.src = offlineImgSrc;
		} else if (this.state.online) { 
			//Tesseract.recognize(localStorage.offlineImg);
		} 
	}

	getBase64Image(img) {
	    // Create an empty canvas element
	    var canvas = document.createElement("canvas");
	    canvas.width = img.width;
	    canvas.height = img.height;

	    // Copy the image contents to the canvas
	    var ctx = canvas.getContext("2d");
	    ctx.drawImage(img, 0, 0);

	    var dataURL = canvas.toDataURL("image/png");

	    return dataURL.replace(/^data:image\/(png|jpg);base64,/, "");
	}

	checkOnline(){
		window.addEventListener('online',  onlineChange.bind(this));
	 	window.addEventListener('offline', onlineChange.bind(this));

	 	function onlineChange(event){
			if(navigator.onLine){
				console.log("ONLINE");
				this.setState({online: true});
				$('select').val("remote");
				this.setState({mode: "remote"});
			}
			else{
				console.log("OFFLINE");
				this.setState({online: false});
				$('select').val("local");
				this.setState({mode: "local"});
			}
		}
	}

	setLogin(loginState) {
		this.setState({logged: loginState});
	}

	logout() {
		localStorage.clear();
		FB.getLoginStatus(function(response) {
		    if (response == "connected") {
				FB.logout(function(response) {
				   // Person is now logged out
				});
		    }
		});
		this.setState({logged: false});
	}

	showOcrItem(item) {
		this.setState({ocrItem: item});
	}

	resetItemState() {
		this.setState({ocrItem: null});
	}

	getPictures(pictures) {
		this.setState({ocrPictures: pictures});
	}

	changeMode(mode){
		this.setState({mode: mode});
	}

	localOCR(pictures, cbEachImage){
        pictures.forEach((img, i) =>{
            Tesseract.recognize(img)
            .then(result => { 
                cbEachImage(i, result.text);
            })
        })  
    }

    remoteOCR(pictures, cbDone){
    	if(pictures.length <= 0){
    		console.log("No pictures to process");
    		return
    	}

    	var images = { "images": pictures};
    	
    	$.ajax({
		    type: "POST",
		    url: config.apiBaseUrl+"ocr/text",
		    contentType: "application/json",
		    dataType: "json",
		    data: JSON.stringify(images),
		    success: function(result){
		    	cbDone(result);
		    },
			beforeSend: function(xhr, settings) { 
				xhr.setRequestHeader('Authorization','Bearer ' + localStorage.bearer_token)
			},  
		    error: function() {
		    	console.log("Remote OCR send error");
		    }
		});
    }

	render () {
		var state;
		var pageName;
		if (!this.state.logged) {
			state = <Login setLogin={this.setLogin.bind(this)}/>
			pageName = "Login";
		} else if (this.state.ocrItem) {
			state = <HistoryItem item={this.state.ocrItem} online={this.state.online}/>
			pageName = "History item"
		} else if (this.state.ocrPictures.length != 0 && this.state.mode === "benchmark") {
			state = <Benchmark pictures={this.state.ocrPictures} localOCR={this.localOCR} remoteOCR={this.remoteOCR} />
			pageName = "Benchmark";
		} else if (this.state.ocrPictures.length != 0) {
			state = <OcrResult pictures={this.state.ocrPictures} localOCR={this.localOCR} remoteOCR={this.remoteOCR} mode={this.state.mode}/>
			pageName = "OCR results"	
		} else {
			state = <Dashboard showOcrItem={this.showOcrItem.bind(this)} getPictures={this.getPictures.bind(this)} changeMode={this.changeMode.bind(this)} mode={this.state.mode} online={this.state.online}/>
			pageName = "Dashboard"
		}

		return (
			<div className="app-container">
				<nav>
					<div className="nav-wrapper">
					{this.state.ocrItem ? 
						<span onClick={this.resetItemState.bind(this)} className="brand-logo left"><i className="material-icons">&#xE5C4;</i></span>
					:
						<a href="/" className="brand-logo left"><i className="material-icons">&#xE88A;</i></a>
					}
						<span className="brand-logo center">{pageName}</span>
						<ul className="right">
							<li><span onClick={this.logout.bind(this)}><i className="material-icons">&#xE879;</i></span></li>
						</ul>
					</div>
				</nav>
				{state}
			</div>
		);
	}
}

ReactDOM.render(<App />, document.querySelector(".app"));
