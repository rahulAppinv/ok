package com.app.okra.bluetooth.bluetooth

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import com.app.okra.bluetooth.BleManager
import com.app.okra.bluetooth.callback.BleWriteCallback
import com.app.okra.bluetooth.data.BleMsg
import com.app.okra.bluetooth.exception.BleException
import com.app.okra.bluetooth.exception.OtherException
import com.app.okra.bluetooth.utils.BleLog
import java.util.*

class SplitWriter {
    private val mHandlerThread: HandlerThread
    private val mHandler: Handler
    private var mBleBluetooth: BleBluetooth? = null
    private var mUuid_service: String? = null
    private var mUuid_write: String? = null
    private var mData: ByteArray?=null
    private var mCount = 0
    private var mSendNextWhenLastSuccess = false
    private var mIntervalBetweenTwoPackage: Long = 0
    private var mCallback: BleWriteCallback? = null
    private var mDataQueue: Queue<ByteArray?>? = null
    private var mTotalNum = 0
    fun splitWrite(
        bleBluetooth: BleBluetooth?,
        uuid_service: String?,
        uuid_write: String?,
        data: ByteArray?,
        sendNextWhenLastSuccess: Boolean,
        intervalBetweenTwoPackage: Long,
        callback: BleWriteCallback?
    ) {
        mBleBluetooth = bleBluetooth
        mUuid_service = uuid_service
        mUuid_write = uuid_write
        mData = data
        mSendNextWhenLastSuccess = sendNextWhenLastSuccess
        mIntervalBetweenTwoPackage = intervalBetweenTwoPackage
        mCount = BleManager.instance.splitWriteNum
        mCallback = callback
        splitWrite()
    }

    private fun splitWrite() {
        requireNotNull(mData) { "data is Null!" }
        require(mCount >= 1) { "split count should higher than 0!" }
        mDataQueue = splitByte(mData!!, mCount)
        mTotalNum = mDataQueue!!.size
        write()
    }

    private fun write() {
        if (mDataQueue!!.peek() == null) {
            release()
            return
        }
        val data = mDataQueue!!.poll()
        mBleBluetooth!!.newBleConnector()
            .withUUIDString(mUuid_service, mUuid_write)
            .writeCharacteristic(
                data,
                object : BleWriteCallback() {
                    override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray?) {
                        val position = mTotalNum - mDataQueue!!.size
                        if (mCallback != null) {
                            mCallback!!.onWriteSuccess(position, mTotalNum, justWrite)
                        }
                        if (mSendNextWhenLastSuccess) {
                            val message = mHandler.obtainMessage(BleMsg.MSG_SPLIT_WRITE_NEXT)
                            mHandler.sendMessageDelayed(message, mIntervalBetweenTwoPackage)
                        }
                    }

                    override fun onWriteFailure(exception: BleException?) {
                        if (mCallback != null) {
                            mCallback!!.onWriteFailure(OtherException("exception occur while writing: " + exception!!.description))
                        }
                        if (mSendNextWhenLastSuccess) {
                            val message = mHandler.obtainMessage(BleMsg.MSG_SPLIT_WRITE_NEXT)
                            mHandler.sendMessageDelayed(message, mIntervalBetweenTwoPackage)
                        }
                    }
                },
                mUuid_write!!
            )
        if (!mSendNextWhenLastSuccess) {
            val message = mHandler.obtainMessage(BleMsg.MSG_SPLIT_WRITE_NEXT)
            mHandler.sendMessageDelayed(message, mIntervalBetweenTwoPackage)
        }
    }

    private fun release() {
        mHandlerThread.quit()
        mHandler.removeCallbacksAndMessages(null)
    }

    companion object {
        //    private static Queue<byte[]> splitByte(byte[] data, int count) {
        //        if (count > 20) {
        //            BleLog.w("Be careful: split count beyond 20! Ensure MTU higher than 23!");
        //        }
        //        Queue<byte[]> byteQueue = new LinkedList<>();
        //        if (data != null) {
        //            int index = 0;
        //            do {
        //                byte[] rawData = new byte[data.length - index];
        //                byte[] newData;
        //                System.arraycopy(data, index, rawData, 0, data.length - index);
        //                if (rawData.length <= count) {
        //                    newData = new byte[rawData.length];
        //                    System.arraycopy(rawData, 0, newData, 0, rawData.length);
        //                    index += rawData.length;
        //                } else {
        //                    newData = new byte[count];
        //                    System.arraycopy(data, index, newData, 0, count);
        //                    index += count;
        //                }
        //                byteQueue.offer(newData);
        //            } while (index < data.length);
        //        }
        //        return byteQueue;
        //    }
        private fun splitByte(data: ByteArray, count: Int): Queue<ByteArray?> {
            if (count > 20) {
                BleLog.w("Be careful: split count beyond 20! Ensure MTU higher than 23!")
            }
            val byteQueue: Queue<ByteArray?> = LinkedList()
            val pkgCount: Int
            pkgCount = if (data.size % count == 0) {
                data.size / count
            } else {
                Math.round((data.size / count + 1).toFloat())
            }
            if (pkgCount > 0) {
                for (i in 0 until pkgCount) {
                    var dataPkg: ByteArray?
                    var j: Int
                    if (pkgCount == 1 || i == pkgCount - 1) {
                        j = if (data.size % count == 0) count else data.size % count
                        System.arraycopy(data, i * count, ByteArray(j).also { dataPkg = it }, 0, j)
                    } else {
                        System.arraycopy(
                            data,
                            i * count,
                            ByteArray(count).also { dataPkg = it },
                            0,
                            count
                        )
                    }
                    byteQueue.offer(dataPkg)
                }
            }
            return byteQueue
        }
    }

    init {
        mHandlerThread = HandlerThread("splitWriter")
        mHandlerThread.start()
        mHandler = object : Handler(mHandlerThread.looper) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                if (msg.what == BleMsg.MSG_SPLIT_WRITE_NEXT) {
                    write()
                }
            }
        }
    }
}