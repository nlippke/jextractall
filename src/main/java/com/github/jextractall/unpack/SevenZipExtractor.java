package com.github.jextractall.unpack;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.github.jextractall.exceptions.CRCException;
import com.github.jextractall.exceptions.DataErrorException;
import com.github.jextractall.exceptions.IncorrectPasswordException;
import com.github.jextractall.exceptions.UnknownCompressionException;
import com.github.jextractall.exceptions.UnknownOperationResultException;
import com.github.jextractall.ui.i18n.Messages;
import com.github.jextractall.unpack.ExtractionResult.STATUS;
import com.github.jextractall.unpack.common.FileAdvisor;
import com.github.jextractall.unpack.common.Result.ResultBuilder;

import net.sf.sevenzipjbinding.ArchiveFormat;
import net.sf.sevenzipjbinding.ExtractAskMode;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.IArchiveOpenCallback;
import net.sf.sevenzipjbinding.IArchiveOpenVolumeCallback;
import net.sf.sevenzipjbinding.ICryptoGetTextPassword;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.IInStream;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

public class SevenZipExtractor implements Extractor {

	private ExtractorCallback callback;
	private ResultBuilder resultBuilder;
	private long totalSize;
	private boolean cancelFlag;

	private Set<OutputStream> targetOutputStreams;

	private static HashMap<String, ArchiveFormat> FORMAT = new HashMap<>();
	static {
		FORMAT.put("zip", ArchiveFormat.ZIP);
		FORMAT.put("tar", ArchiveFormat.TAR);
		FORMAT.put("rar", ArchiveFormat.RAR);
		FORMAT.put("lz", ArchiveFormat.LZH);
		FORMAT.put("iso", ArchiveFormat.ISO);
		FORMAT.put("hfs", ArchiveFormat.HFS);
		FORMAT.put("gz", ArchiveFormat.GZIP);
		FORMAT.put("cpio", ArchiveFormat.CPIO);
		FORMAT.put("bz2", ArchiveFormat.BZIP2);
		FORMAT.put("7z", ArchiveFormat.SEVEN_ZIP);
		FORMAT.put("z", ArchiveFormat.Z);
		FORMAT.put("arj", ArchiveFormat.ARJ);
		FORMAT.put("cab", ArchiveFormat.CAB);
		FORMAT.put("lzh", ArchiveFormat.LZH);
		FORMAT.put("nsis", ArchiveFormat.NSIS);
		FORMAT.put("deb", ArchiveFormat.DEB);
		FORMAT.put("rpm", ArchiveFormat.RPM);
		FORMAT.put("udf", ArchiveFormat.UDF);
		FORMAT.put("win", ArchiveFormat.WIM);
		FORMAT.put("xar", ArchiveFormat.XAR);
	}

	/** {@inheritDoc} */
	@Override
	public ExtractionResult extractArchive(Path pathToArchive, ExtractorCallback callback) {
		this.callback = callback;
		this.cancelFlag = false;

		resultBuilder = ResultBuilder.newInstance();

		targetOutputStreams = new HashSet<OutputStream>();

		try (RandomAccessFile raf = new RandomAccessFile(pathToArchive.toFile(), "r");
				ArchiveOpenVolumeCallback archiveOpenVolumeCallback = new ArchiveOpenVolumeCallback();
				IInStream inStream = archiveOpenVolumeCallback.getStream(pathToArchive.toString());

				IInArchive inArchive = SevenZip.openInArchive(
						FORMAT.get(getFileExtension(pathToArchive.getFileName().toString())), inStream,
						archiveOpenVolumeCallback);) {

			int[] in = new int[inArchive.getNumberOfItems()];
			for (int i = 0; i < in.length; i++) {
				in[i] = i;
			}

			inArchive.extract(in, false, new ArchiveExtractCallback(inArchive, pathToArchive.getFileName().toString()));

		} catch (Exception ex) {
			resultBuilder.withException(ex);
		} finally {
			for (OutputStream os : targetOutputStreams) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		if (cancelFlag) {
			resultBuilder.withResult(STATUS.ABORT);
		}

		return resultBuilder.create();

	}

	private String getFileExtension(String fName) {
		int pos = fName.lastIndexOf('.');
		if (pos > 0) {
			return fName.substring(pos + 1).toLowerCase();
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public boolean canExtract(Path pathToArchive) {
		String archiveName = pathToArchive.getFileName().toString().toLowerCase();
		if (archiveName.matches("(?i).*[.]part[\\d]+[.]rar")) {
			if (!archiveName.matches("(?i).*[.]part[0]*1[.]rar")) {
				return false;
			}
		}
		try (RandomAccessFile raf = new RandomAccessFile(pathToArchive.toFile(), "r");
				IInArchive inArchive = SevenZip.openInArchive(null, new RandomAccessFileInStream(raf),
						new DummyArchiveOpenCallback());) {
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String[] getSupportedExtensions() {
		return FORMAT.keySet().toArray(new String[0]);
	}

	class ArchiveExtractCallback implements IArchiveExtractCallback, ICryptoGetTextPassword {
		private IInArchive inArchive;
		private String archiveName;
		private String usedPassword;

		public ArchiveExtractCallback(IInArchive inArchive, String archiveName) {
			this.inArchive = inArchive;
			this.archiveName = archiveName;
		}

		public ISequentialOutStream getStream(int index, ExtractAskMode extractAskMode) throws SevenZipException {
			if (extractAskMode != ExtractAskMode.EXTRACT) {
				return null;
			}

			if (inArchive.getSimpleInterface().getArchiveItem(index).isFolder()) {
				return null;
			}

			String fName = inArchive.getStringProperty(index, PropID.PATH);
			if (StringUtils.isEmpty(fName)) {
				int idx = archiveName.lastIndexOf('.');
				if (idx > 0) {
					fName = archiveName.substring(0, idx);
				} else {
					fName = archiveName + ".ext";
				}
			}
			FileAdvisor advice = callback.advice(fName);
			try {
				if (advice.skip()) {
					return null;
				}
				return new ExtractedFileOutputStream(advice.getPath());

			} catch (IOException e) {
				throw new SevenZipException(e);
			}
		}

		public void prepareOperation(ExtractAskMode extractAskMode) throws SevenZipException {
		}

		public void setOperationResult(ExtractOperationResult extractOperationResult) throws SevenZipException {
			switch (extractOperationResult) {
			case CRCERROR:
				resultBuilder.withException(new CRCException());
				break;
			case DATAERROR:
				resultBuilder.withException(
						usedPassword != null ? new IncorrectPasswordException(usedPassword) : new DataErrorException());
				break;
			case UNSUPPORTEDMETHOD:
				resultBuilder.withException(new UnknownCompressionException());
				break;
			case UNKNOWN_OPERATION_RESULT:
				resultBuilder.withException(new UnknownOperationResultException());
				break;
			default:
			}
		}

		public void setCompleted(long completeValue) throws SevenZipException {
			callback.volumeProgress(null, completeValue, totalSize);
		}

		public void setTotal(long total) throws SevenZipException {
			totalSize = total;
		}

		@Override
		public String cryptoGetTextPassword() throws SevenZipException {
			usedPassword = callback.getPassword();
			return usedPassword;
		}
	}

	class ExtractedFileOutputStream implements ISequentialOutStream {

		OutputStream fos;

		public ExtractedFileOutputStream(Path targetFile) throws IOException {
			resultBuilder.withExtractedFile(targetFile);
			Path directory = targetFile.getParent();
			if (!Files.exists(directory)) {
				Files.createDirectories(directory);
			}

			fos = Files.newOutputStream(targetFile);
			targetOutputStreams.add(fos);
		}

		/** {@inheritDoc} */
		@Override
		public int write(byte[] data) throws SevenZipException {
			try {
				if (cancelFlag) {
					throw new ClosedByInterruptException();
				}
				fos.write(data);
				return data.length;
			} catch (ClosedByInterruptException e) {
				throw new SevenZipException(Messages.getMessage("error.interruptException"), e);
			} catch (IOException e) {
				throw new SevenZipException(Messages.getMessage("error.writeData"), e);
			}
		}
	}

	class DummyArchiveOpenCallback implements IArchiveOpenCallback {
		@Override
		public void setTotal(Long files, Long bytes) throws SevenZipException {
		}

		@Override
		public void setCompleted(Long files, Long bytes) throws SevenZipException {
		}
	}

	class ArchiveOpenVolumeCallback
			implements IArchiveOpenVolumeCallback, IArchiveOpenCallback, AutoCloseable, ICryptoGetTextPassword {

		private Map<String, RandomAccessFile> openedRandomAccessFileList = new HashMap<String, RandomAccessFile>();

		private String name;

		/**
		 * This method should at least provide the name of the last opened volume
		 * (propID=PropID.NAME).
		 *
		 * @see IArchiveOpenVolumeCallback#getProperty(PropID)
		 */
		public Object getProperty(PropID propID) throws SevenZipException {
			switch (propID) {
			case NAME:
				return name;
			default:
				return null;
			}
		}

		/**
		 * The name of the required volume will be calculated out of the name of the
		 * first volume and a volume index. In case of RAR file, the substring
		 * ".partNN." in the name of the volume file will indicate a volume with id NN.
		 * For example:
		 * <ul>
		 * <li>test.rar - single part archive or multi-part archive with a single
		 * volume</li>
		 * <li>test.part23.rar - 23-th part of a multi-part archive</li>
		 * <li>test.part001.rar - first part of a multi-part archive. "00" indicates,
		 * that at least 100 volumes must exist.</li>
		 * </ul>
		 */
		public IInStream getStream(String filename) throws SevenZipException {
			try {
				// We use caching of opened streams, so check cache first
				RandomAccessFile randomAccessFile = openedRandomAccessFileList.get(filename);
				name = filename;
				if (randomAccessFile != null) { // Cache hit.
					randomAccessFile.seek(0);
					return new RandomAccessFileInStream(randomAccessFile);
				}

				randomAccessFile = new RandomAccessFile(filename, "r");

				openedRandomAccessFileList.put(filename, randomAccessFile);
				resultBuilder.withVolumneFile(FileSystems.getDefault().getPath(filename));

				return new RandomAccessFileInStream(randomAccessFile);
			} catch (FileNotFoundException fileNotFoundException) {
				return null;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		/**
		 * Close all opened streams
		 */
		public void close() throws IOException {
			for (RandomAccessFile file : openedRandomAccessFileList.values()) {
				file.close();
			}
		}

		/** {@inheritDoc} */
		public void setCompleted(Long files, Long bytes) throws SevenZipException {
		}

		/** {@inheritDoc} */
		public void setTotal(Long files, Long bytes) throws SevenZipException {
		}

		@Override
		public String cryptoGetTextPassword() throws SevenZipException {
			return callback.getPassword();
		}
	}

	@Override
	public void cancel() {
		this.cancelFlag = true;
	}

}
