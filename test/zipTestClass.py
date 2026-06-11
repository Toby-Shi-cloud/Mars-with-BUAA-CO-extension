import os
import sys
import zipfile

if __name__ == '__main__':
    with zipfile.ZipFile(sys.argv[1], 'w', zipfile.ZIP_DEFLATED) as zip:
        for root, _, files in os.walk('.'):
            for item in sorted(files):
                if item.endswith(('.class', '.asm', '.java', '.txt', '.out', '.bat')):
                    zip.write(os.path.join(root, item))
