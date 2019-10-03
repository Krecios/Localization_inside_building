import flask
from flask import Flask
from flask import Request
import werkzeug
app = Flask(__name__)

@app.route('/')
def hello_world():
    return 'Hello, World!'

@app.route('/connect', methods=['GET', 'POST'])
def ResponseMessageConnect():
    return 'You have connected to the Flask server.'

@app.route('/upload', methods=['GET', 'POST'])
def ResponseMessageUpload():
    UploadedImage = flask.request.files['image']
    filename = werkzeug.utils.secure_filename(UploadedImage.filename)
    print('\nRecieved an image named : ' + UploadedImage.filename)
    UploadedImage.save(filename)
    return "Image has been saved"