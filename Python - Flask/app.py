import flask
from flask import Flask
from flask import Request
import werkzeug
import cv2
import subprocess
app = Flask(__name__)


def CutTheVideo():
    vidcap = cv2.VideoCapture('UploadedVideo.mp4')
    success, image = vidcap.read()
    count = 0
    blackpercent = 0
    size = 1920*1080
    while success:
        if count % 1 == 0:
            if count == 0:
                im1 = image
                cv2.imwrite('./Frames/frame%d.jpg' %count, image)     # save frame as JPEG file
            elif count != 0:
                im2 = image
                im3 = cv2.subtract(im2, im1)
                testimage = cv2.cvtColor(im3, cv2.COLOR_BGR2GRAY)
                (thresh, testimage) = cv2.threshold(testimage, 10, 255, cv2.THRESH_BINARY)
                blackpercent = 1-(cv2.countNonZero(testimage)/size)
                print(blackpercent)
                cv2.imwrite('./Test/TEST%d.jpg' %count, testimage)
                if blackpercent < 0.75:
                    #image = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
                    cv2.imwrite('./Frames/frame%d.jpg' %count, image)     # save frame as JPEG file
                    print('Frame saved: Frame no. %d' %count)
                    im1 = cv2.imread('./Frames/frame%d.jpg' %count)
        success, image = vidcap.read()
        count += 1


@app.route('/')
def hello_world():
    return 'Hello, World!'


@app.route('/connect', methods=['GET', 'POST'])
def ResponseMessageConnect():
    return 'You have connected to the Flask server.'


@app.route('/upload', methods=['GET', 'POST'])
def ResponseMessageUpload():
    UploadedImage = flask.request.files['video']
    filename = werkzeug.utils.secure_filename(UploadedImage.filename)
    print('\nRecieved an image named : ' + UploadedImage.filename)
    UploadedImage.save(filename)
    return "Image has been saved"


@app.route('/cut', methods=['GET', 'POST'])
def CutVideo():
    CutTheVideo()
    return 'Video has been cut.'
