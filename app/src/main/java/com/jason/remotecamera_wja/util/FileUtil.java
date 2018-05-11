package com.jason.remotecamera_wja.util;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.jason.remotecamera_wja.app.Constant;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 文件工具类
 */
public class FileUtil {

	private static final String TAG = "FileUtil";
	private static  String storagePath = "";

	private static String initPath(){
		if(storagePath.equals("")){
			storagePath = Constant.picturePath;
			File f = new File(storagePath);
			if(!f.exists()){
				f.mkdir();
			}
		}
		return storagePath;
	}


	public static void saveBitmap(Bitmap b) {

		String path = initPath();
		long dataTake = System.currentTimeMillis();
		String jpegName = path + "/" + dataTake + ".jpg";
		Log.i(TAG, "saveBitmap:jpegName = " + jpegName);
		try {
			FileOutputStream fout = new FileOutputStream(jpegName);
			BufferedOutputStream bos = new BufferedOutputStream(fout);
			b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			bos.flush();
			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 删除文件
	 *
	 * @param filePath
	 * @return
	 */
	public static boolean deleteFile(String filePath) {

		if (!TextUtils.isEmpty(filePath)) {
			final File file = new File(filePath);
			if (file.exists()) {
				return file.delete();
			}
		}
		return false;
	}

	/**
	 * 删除文件夹下所有文件
	 *
	 * @return
	 */
	public static void deleteDirectoryAllFile(String directoryPath) {
		final File file = new File(directoryPath);
		deleteDirectoryAllFile(file);
	}

	public static void deleteDirectoryAllFile(File file) {
		if (!file.exists()) {
			return;
		}

		boolean rslt = true;// 保存中间结果
		if (!(rslt = file.delete())) {// 先尝试直接删除
			// 若文件夹非空。枚举、递归删除里面内容
			final File subs[] = file.listFiles();
			final int size = subs.length - 1;
			for (int i = 0; i <= size; i++) {
				if (subs[i].isDirectory())
					deleteDirectoryAllFile(subs[i]);// 递归删除子文件夹内容
				rslt = subs[i].delete();// 删除子文件夹本身
			}
			// rslt = file.delete();// 删除此文件夹本身
		}

		if (!rslt) {

			return;
		}
	}

	/**
	 * 根据后缀名删除文件
	 */
	public static boolean deleteEndFile(String delPath, String delEndName) {
		// param is null
		if (delPath == null || delEndName == null) {
			return false;
		}
		try {
			// create file
			final File file = new File(delPath);
			if (file != null) {
				if (file.isDirectory()) {
					// file list
					String[] fileList = file.list();
					File delFile = null;

					// digui
					final int size = fileList.length;
					for (int i = 0; i < size; i++) {
						// create new file
						delFile = new File(delPath + "/" + fileList[i]);
						if (delFile != null && delFile.isFile()) {// 删除该文件夹下所有文件以delEndName为后缀的文件（不包含子文件夹里的文件）
							// if (delFile != null) {//
							// 删除该文件夹下所有文件以delEndName为后缀的文件（包含子文件夹里的文件）
							deleteEndFile(delFile.toString(), delEndName);
						} else {
							// nothing
						}
					}
				} else if (file.isFile()) {

					// check the end name
					if (file.toString().contains(".")
							&& file.toString()
							.substring(
									(file.toString().lastIndexOf(".") + 1))
							.equals(delEndName)) {
						// file delete
						file.delete();
					}
				}
			}
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	/**
	 * 删除文件夹内所有文件
	 *
	 * @param delpath
	 *            delpath path of file
	 * @return boolean the result
	 */
	public static boolean deleteAllFile(String delpath) {
		try {
			// create file
			final File file = new File(delpath);

			if (!file.isDirectory()) {
				file.delete();
			} else if (file.isDirectory()) {

				final String[] filelist = file.list();
				final int size = filelist.length;
				for (int i = 0; i < size; i++) {

					// create new file
					final File delfile = new File(delpath + "/" + filelist[i]);
					if (!delfile.isDirectory()) {
						delfile.delete();
					} else if (delfile.isDirectory()) {
						// digui
						deleteFile(delpath + "/" + filelist[i]);
					}
				}
				file.delete();
			}
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	/**
	 * 删除目录（文件夹）以及目录下的文件
	 *
	 * @param sPath
	 *            被删除目录的文件路径
	 * @return 目录删除成功返回true，否则返回false
	 */
	public static boolean deleteDirectory(String sPath) {

		if (TextUtils.isEmpty(sPath)) {
			return false;
		}

		boolean flag;
		// 如果sPath不以文件分隔符结尾，自动添加文件分隔符
		if (!sPath.endsWith(File.separator)) {
			sPath = sPath + File.separator;
		}
		final File dirFile = new File(sPath);
		// 如果dir对应的文件不存在，或者不是一个目录，则退出
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}
		flag = true;
		// 删除文件夹下的所有文件(包括子目录)
		final File[] files = dirFile.listFiles();
		if (files != null && files.length > 0) {
			for (int i = 0; i < files.length; i++) {
				// 删除子文件
				if (files[i].isFile()) {
					flag = deleteFile(files[i].getAbsolutePath());
					if (!flag)
						break;
				} // 删除子目录
				else {
					flag = deleteDirectory(files[i].getAbsolutePath());
					if (!flag)
						break;
				}
			}
		}
		if (!flag)
			return false;
		// 删除当前目录
		if (dirFile.delete()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 获取后缀名
	 *
	 * @param path
	 *            全路径
	 * @return
	 */
	public static String getFileExtName(String path) {
		String ext = "";
		if ((path != null) && (path.length() > 0)) {
			int dot = path.lastIndexOf('.');
			if ((dot > -1) && (dot < (path.length() - 1))) {
				ext = path.substring(dot + 1);
			}
		}
		return ext;
	}

	/**
	 * 获取文件名
	 *
	 * @param path
	 *            全路径
	 * @return
	 */
	public static String getFileName(String path) {
		if (!TextUtils.isEmpty(path)) {
			return path.substring(path.lastIndexOf(File.separator) + 1);
		}
		return "";
	}



}
