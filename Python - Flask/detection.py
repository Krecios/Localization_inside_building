from imageai.Detection import ObjectDetection
import cv2
import os

rectangles = []
detector = ObjectDetection()

model_path = './models/yolo-tiny.h5'

imagesArray = os.listdir('./Frames')

for i in imagesArray:
    image = cv2.imread('./Frames/' + i)
    detector.setModelTypeAsTinyYOLOv3()
    detector.setModelPath(model_path)
    detector.loadModel()

    customObject = detector.CustomObjects(person=True)
    detection = detector.detectCustomObjectsFromImage(custom_objects=customObject, input_image=('./input/' + i), output_image_path=('./DetectionOutput/output-' + i)
        , minimum_percentage_probability=40)

    for eachItem in detection:
        print(eachItem['name'], ':', eachItem['percentage_probability'], '            ', eachItem['box_points'][0])

        rectangles.append(eachItem['box_points'])

    th, image = cv2.threshold(image, 255, 255, cv2.THRESH_BINARY_INV)
    size = len(rectangles)
    while(size != 0):
        size = size - 1
        x1 = rectangles[size][0]
        y1 = rectangles[size][1]
        x2 = rectangles[size][2]
        y2 = rectangles[size][3]
        cv2.rectangle(image, (int(x1 * 0.98), int(y1 * 0.98)), (int(x2 * 1.02), int(y2 * 1.02)), (0,0,0), -1)
    cv2.imwrite('./Mask/mask-' + i, image)
    rectangles.clear()
