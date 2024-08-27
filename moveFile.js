import { registerPlugin } from '@capacitor/core';

const MoveFile = registerPlugin('MoveFile');

function moveFile(sourcePath, fileName, fileType)  {
	if (sourcePath.startsWith('file://')) {
		sourcePath = sourcePath.replace('file://', '');
	}

	MoveFile.moveFile({ sourcePath, fileName, fileType });
}

export default moveFile;
