import React, { Component } from 'react';
import FileInput from "./file-input";
import config from "../config";

export default class Dashboard extends Component {
	constructor (props) {
		super(props);
		this.state = {history: [], newHistoryLoaded: false};
	}

	componentDidMount() {
		this.getHistory();
		//Need to do it that way, because of material
		$('select').material_select(this.handleChange.bind(this));
	}

	componentWillReceiveProps(prevProps) {
		if (prevProps.online != this.props.online) {
			this.setState({newHistoryLoaded: false});
		}
		$('select').material_select(this.handleChange.bind(this));
	}

	componentDidUpdate(prevProps) {
		if (prevProps.online != this.props.online) {
			this.getHistory();
		}
	}

	getHistory() {
		if(this.props.online){
			$.ajax({
			    type: "GET",
			    url: config.apiBaseUrl+"ocr/history",
			    contentType: "application/json",
			    dataType: "json",
			    success: function(result) {
			    	var historyWithoutImg = result.map((item, key) =>{
			    		var itemWithoutImg = {};
			    		itemWithoutImg.ocr = item.ocr;
			    		itemWithoutImg.creationTime = item.creationTime;
			    		return itemWithoutImg;
			    	});
			    	localStorage.setItem("history", JSON.stringify(historyWithoutImg));
			    	this.setState({history: result.reverse(), newHistoryLoaded: true});
			    }.bind(this),
			    beforeSend: function(xhr, settings) { 
					xhr.setRequestHeader('Authorization','Bearer ' + localStorage.bearer_token)
				},  
			    error: function(err) {
			    	console.log("History get error", err);
			    }
			});
		}
		else{
			var history = JSON.parse(localStorage.history);
			if(history){
				console.log("Get local history");
				this.setState({history: history, newHistoryLoaded: true});
			}
		}
	}

	handleChange() {
		//Can't get target value because of material (renders as list, not select with options)
		var mode = event.target.innerText.toLowerCase()
		if(mode === "local" || mode === "remote" || mode === "benchmark") {
    		this.props.changeMode(mode);
		}
    }

	renderHistory() {
		return this.state.history.map((item, i) => {
			return (
				<div className="card horizontal" key={i} onClick={this.props.showOcrItem.bind(this, item)}>
					{this.props.online && item.thumbnails.length != 0 ? 
						item.thumbnails.map((thumb, i) => {
					    	var imgWidth = {width: (128 / item.thumbnails.length) + "px"};
					    	var imgSrc = "data:image/jpeg;base64,"+thumb;
		        			return (
		        				<div className="card-image" style={imgWidth} key={i}>
							        <img src={imgSrc}/>
							    </div>
		        			)	
		        		})
					:
						<div className="card-image">
							<img src={"data:image/jpeg;base64,"+localStorage.offlineImg}/>
						</div>

					}
			      <div className="card-stacked">
			        <div className="card-content">
			        	<div>
			        		{item.ocr.map((text, i) => {
			        			return (
			        				<p key={i}>{text}</p>
			        			)	
			        		})}
			        	</div>
			        </div>
			      </div>
			    </div>
		    )
	   	})
	}


	render() {
		var history;
		if (this.state.history.length == 0 && this.state.newHistoryLoaded) {
			history = <p className="center">No history yet</p>;
		}
		else if (this.state.newHistoryLoaded) {
			history = this.renderHistory()
		}

		return (
		  <div className="container dashboard">
		  	<div className="row">
				<div className="col s12 m6 offset-m3">
				 <div className="input-field col s12">
				    <select defaultValue={this.props.mode}>
				      <option value="" disabled>Choose your option</option>
				      <option value="local">Local</option>
				      <option value="remote" disabled={!this.props.online}>Remote</option>
				      <option value="benchmark" disabled={!this.props.online}>Benchmark</option>
				    </select>
				    <label>Choose OCR method:</label>
				  </div>
				</div>
		  	</div>
		  	<div className="row">
		  		<div className="col s12 m6 offset-m3">
					<div className="row">
						<div className="col s6 center">
							<FileInput type="camera" getPictures={this.props.getPictures} />
						</div>
						<div className="col s6 center">
							<FileInput type="file" getPictures={this.props.getPictures} />
						</div>
			  		</div>
		  		</div>
		  	</div>
		  	<div className="row">
				<h4 className="center">OCR history</h4>
				<div className="history">
					{history}
				</div>
		  	</div>
		  </div>
		);
	}
}
