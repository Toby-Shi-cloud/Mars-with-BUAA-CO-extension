import os
import sys
import zipfile

if __name__ == '__main__':
    zip = zipfile.ZipFile(sys.argv[1], 'w', zipfile.ZIP_DEFLATED)
    for item in os.listdir('.'):
        if item.endswith(('.class', '.asm', '.java', ".txt")):
            zip.write(os.path.join('.', item))
    zip.close()
