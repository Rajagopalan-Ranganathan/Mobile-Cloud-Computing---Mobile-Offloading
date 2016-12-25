import React, { Component } from 'react';

export default class Benchmark extends Component {
	constructor (props) {
		super(props);

		if(this.props.pictures && this.props.pictures.length > 0){
            this.state = {loadingLocal: true, loadingRemote: true}
        }

		//Init array for times
		this.imageTimes = new Array(2);
		for (var i = 0; i < this.imageTimes.length; i++) {
		  this.imageTimes[i] = new Array(this.props.pictures.length + 1);
		}
	}

	componentWillMount(){
		this.performLocalOcr();
		this.performRemoteOcr();
	}

	performLocalOcr(){
		this.imageTimes[0][0] = performance.now()
	    this.props.localOCR(this.props.pictures, this.localCbEachImage.bind(this));
        }

	performRemoteOcr(){
		//Calculate exchanged data
		this.imageSizes = new Array(this.props.pictures.length+1);
		var sum = 0.0;
		this.props.pictures.forEach((img, index) =>{
			this.imageSizes[index + 1] = 4 * Math.ceil(img.length.toFixed(1) / 3);
			sum += this.imageSizes[index + 1];
		});
		this.imageSizes[0] = sum;
		this.analyzeResult("remote", this.imageSizes, "size");

		this.props.remoteOCR(this.props.pictures, this.remoteCbDone.bind(this));
	}

	localCbEachImage(index, text){
		this.imageTimes[0][index+1] = performance.now()
		
		if(this.state.localText && this.state.localText.length > 0){
            this.setState({localText: this.state.localText + " " + text});
        }
        else{
            this.setState({localText: text});
        }

		if(index + 1 >= this.props.pictures.length){
            this.setState({loadingLocal: false})
        }

		if(index + 1 == this.props.pictures.length){
			var diffArray = new Array(this.imageTimes[0].length);

			this.imageTimes[0].forEach((time, index) =>{
				if(index == 0){
					diffArray[0] = this.imageTimes[0][this.imageTimes[0].length - 1] - this.imageTimes[0][0];
				}
				else{
					diffArray[index] = this.imageTimes[0][index] - this.imageTimes[0][index - 1];
				}
			})
			this.analyzeResult("local", diffArray);
		}
	}

	//Remote processing done
    remoteCbDone(result){
    	var ocrtext = "";
        result.ocr.forEach(text =>{
            ocrtext += text;
        });
        var sum = 0.0
        result.benchmarks.forEach((time, index) =>{
        	this.imageTimes[1][index + 1] = time;
        	sum += time
        });
        this.imageTimes[1][0] = sum;

        this.analyzeResult("remote", this.imageTimes[1]);
        this.setState({loadingRemote: false, remoteText: ocrtext});
    }

	analyzeResult(mode, values, valueType = "time"){
		var count = values.length-1;
		var diff = values[0];
		var min = -1;
		var minIndex = -1;
		var max = -1;
		var maxIndex = -1;
		var avg = (diff / count).toFixed(0);
		var dev = 0;
		var diffImage = 0.0

		for(var i = 1; i < values.length; i++){
			diffImage = values[i];

			dev += Math.pow(diffImage - avg, 2);
			if(min < 0){
				min = diffImage;
				minIndex = i-1
			}
			else if(diffImage < min){
				min = diffImage;
				minIndex = i-1
			}

			if(max < 0){
				max = diffImage;
				maxIndex = i-1
			}
			else if(diffImage > max){
				max = diffImage;
				maxIndex = i-1
			}
		}

		dev = Math.sqrt(dev / count).toFixed(0);
		if(mode === "local" && valueType === "time"){
			this.setState({local: {diff: diff.toFixed(0), avg: avg, dev: dev, min: min.toFixed(0), minIndex: minIndex, max: max.toFixed(0), maxIndex: maxIndex}});
		}
		else if(mode === "remote"){
			if(valueType === "time"){
				this.setState({remote: {diff: diff.toFixed(0), avg: avg, dev: dev, min: min.toFixed(0), minIndex: minIndex, max: max.toFixed(0), maxIndex: maxIndex}});
			}
			else if(valueType === "size"){
				this.setState({remoteSize: {diff: diff.toFixed(0), avg: avg, dev: dev, min: min.toFixed(0), minIndex: minIndex, max: max.toFixed(0), maxIndex: maxIndex}});
			}
		}
	}

	render() {
		return (
		  <div className="container benchmark">
		  	<div className="row">
				<h3>Benchmark</h3>
				<p>Number of processed images: {this.props.pictures.length}</p>
				<h5>Local Mode</h5>
				{this.state.loadingLocal &&
                <div className="load-block">
                    <div className="loader"></div>
                    <p>Performing local OCR </p>
                </div>
                }   
                {this.state.local && this.props.pictures.length == 1 &&
				<p>Processing time: {this.state.local.diff} ms<br />
				</p>
				}
				{this.state.local && this.props.pictures.length > 1 &&
				<p>Processing time: {this.state.local.avg} ({this.state.local.dev}) ms<br />
					Minimum: {this.state.local.min} ms ({this.state.local.minIndex}); Maximum: {this.state.local.max} ms ({this.state.local.maxIndex})<br />
				</p>
				}

				<h5>Remote</h5>
				{this.state.loadingRemote &&
                <div className="load-block">
                    <div className="loader"></div>
                    <p>Performing remote OCR </p>
                </div>
                }   
                {this.state.remote && this.props.pictures.length == 1 &&
				<p>Processing time: {this.state.remote.diff} ms<br />
					Exchanged data: {this.state.remoteSize.diff} bytes<br />
				</p>
				}
				{this.state.remote && this.props.pictures.length > 1 &&
				<p>Processing time: {this.state.remote.avg} ({this.state.remote.dev}) ms<br />
					Minimum: {this.state.remote.min} ms ({this.state.remote.minIndex}); Maximum: {this.state.remote.max} ms ({this.state.remote.maxIndex})<br />
				Exchanged data: {this.state.remoteSize.avg} ({this.state.remoteSize.dev}) bytes<br />
					Minimum: {this.state.remoteSize.min} bytes ({this.state.remoteSize.minIndex}); Maximum: {this.state.remoteSize.max} bytes ({this.state.remoteSize.maxIndex})<br />
				</p>
				}
		  	</div>
		  </div>
		);
	}
}