import shutil
import os

folderList = []

folderFrames = './Frames'
folderTest = './Test'
folderMask = './Mask'
folderOutput = './DetectionOutput'

folderList.append(folderMask)
folderList.append(folderOutput)
folderList.append(folderTest)
folderList.append(folderFrames)

for currentFolder in folderList:
    for filename in os.listdir(currentFolder):
        filePath = os.path.join(currentFolder, filename)
        try:
            if os.path.isfile(filePath) or os.path.islink(filePath):
                os.unlink(filePath)
            elif os.path.isdir(filePath):
                shutil.rmtree(filePath)
        except Exception as e:
            print('ERROR!!! File %s. Reason: %s' % (filePath,e))