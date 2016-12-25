import React, { Component } from 'react';

export default class HistoryItem extends Component {
	constructor (props) {
		super(props);

		this.state = {showAllImgs: false};
	}

	toggleShowAllImgs() {
		this.setState({showAllImgs: !this.state.showAllImgs});
	}

	render() {
		var item = this.props.item;
		var date = new Date(this.props.item.creationTime).toString();
		var ocrText = "";
        item.ocr.forEach(text =>{
            ocrText += text;
        });
		var imgs;
		if (!this.props.online || !item.sources) {
			imgs = null;
		}
		else if (!this.state.showAllImgs) {
			imgs = <div className="card-image">
				        <img src={"data:image/jpeg;base64,"+item.sources[0]}/>
				    </div>
		} else {
			imgs = item.sources.map((source, i) => {
    			return (
    				<div className="card-image" key={i}>
				        <img src={"data:image/jpeg;base64,"+source}/>
				    </div>
    			)	
    		})
		} 

		return (
		  <div className="container history-item">
			  <div className="row">
		        <div className="col s12 m6 offset-m3">
		          <div className="toolbar">
		        	  <div className="center p-10">Created: {date}</div>
		              <a className="waves-effect waves-light btn" href={"data:text/plain;charset=utf-8,"+encodeURIComponent(ocrText)} download="ocrtext.txt"><i className="material-icons left">&#xE2C4;</i>Save text</a>
		              <a className="waves-effect waves-light btn" onClick={this.toggleShowAllImgs.bind(this)}><i className="material-icons left">&#xE413;</i>All images</a>
		          </div>
		          <div className="card">
		          	<div className="card-content">
		              	<p>{ocrText}</p>
		            </div>
					{imgs}
		          </div>
		        </div>
		      </div>
		  </div>
		);
	}
}
