import React, { Component } from 'react';
import config from "../config";


export default class OcrResult extends Component {
    constructor (props) {
        super(props);
        if(this.props.pictures && this.props.pictures.length > 0){
            this.state = {loading: true, imageIndex: 1, showAllImgs: false}
        }

        if(this.props.mode && this.props.mode === "local"){
            this.props.localOCR(this.props.pictures, this.cbEachImage.bind(this));
        }
        else if(this.props.mode && this.props.mode === "remote"){
            this.props.remoteOCR(this.props.pictures, this.cbDone.bind(this));
        }
    }

    cbEachImage(index, text){
        if(this.state.ocrtext && this.state.ocrtext.length > 0){
            this.setState({ocrtext: this.state.ocrtext + " " + text});
        }
        else{
            this.setState({ocrtext: text});
        }

        if(index + 1 >= this.props.pictures.length){
            //Finished processing
            this.setState({loading: false, imageIndex: undefined})
        }
        else{
            this.setState({imageIndex: index + 2})
        }
    }

    //Remote processing done
    cbDone(result){
        var ocrtext = "";
        result.ocr.forEach(text =>{
            ocrtext += text;
        });
        this.setState({loading: false, ocrtext: ocrtext});
    }

    toggleShowAllImgs() {
        this.setState({showAllImgs: !this.state.showAllImgs});
    }

    render() {
        if (this.state.loading) {

            return (
                    <div className="load-block">
                        <div className="loader-info">
                            {this.props.mode === "local" && this.state.imageIndex &&
                                <p>Performing OCR on image {this.state.imageIndex} / {this.props.pictures.length} </p>
                            }
                            {this.props.mode === "remote" &&
                                <p>Performing OCR on remote server</p>
                            }
                        </div>
                        <div className="loader"></div>
                    </div>
            );

        } else {

            return (
              <div className="container ocr-result">
                <div className="row">
                    <div className="col s12 m6 offset-m3">
                      <div className="toolbar">
                          <a className="waves-effect waves-light btn" href={"data:text/plain;charset=utf-8,"+encodeURIComponent(this.state.ocrtext)} download="ocrtext.txt"><i className="material-icons left">&#xE2C4;</i>Save text</a>
                          <a className="waves-effect waves-light btn" onClick={this.toggleShowAllImgs.bind(this)}><i className="material-icons left">&#xE413;</i>All images</a>
                      </div>
                      <div className="card">
                        <div className="card-content">
                            <p>{this.state.ocrtext}</p>
                        </div>
                        {!this.state.showAllImgs ? 
                            <div className="card-image">
                                <img src={this.props.pictures[0]}/>
                            </div>
                            :
                            this.props.pictures.map((pic, i) => {
                                return (
                                    <div className="card-image" key={i}>
                                        <img src={pic}/>
                                    </div>
                                )   
                            })
                        }
                      </div>
                    </div>
                </div>
              </div>
            );

        }
    }
}