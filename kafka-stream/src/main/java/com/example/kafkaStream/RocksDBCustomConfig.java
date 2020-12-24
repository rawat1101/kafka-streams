package com.example.kafkaStream;

import java.util.Map;

import org.apache.kafka.streams.state.RocksDBConfigSetter;
import org.rocksdb.BlockBasedTableConfig;
import org.rocksdb.BloomFilter;
import org.rocksdb.Cache;
import org.rocksdb.Filter;
import org.rocksdb.LRUCache;
import org.rocksdb.Options;
import org.rocksdb.WriteBufferManager;

public class RocksDBCustomConfig implements RocksDBConfigSetter {
	private long capacity = 100 * 1024L * 1024L;
	private int memTableSize = 1024 * 1024;
	private long blockSize = 16 * 1024L;
	private int maxWriteBufferNumber = 2;
	private long bufferSizeBytes = 80 * 1024L * 1024L;
	private boolean cacheIndexAndFilterBlocks = true;
	private boolean pinTopLevelIndexAndFilter = true;
	private Cache cache = new LRUCache(capacity);
	private Filter filter = new BloomFilter();
	private WriteBufferManager writeBufferManager = new WriteBufferManager(bufferSizeBytes, cache);

	@Override
	public void setConfig(final String storeName, final Options options, final Map<String, Object> configs) {
		BlockBasedTableConfig tableConfig = new BlockBasedTableConfig();

		tableConfig.setBlockCache(cache);
		tableConfig.setBlockSize(blockSize);
		tableConfig.setCacheIndexAndFilterBlocks(cacheIndexAndFilterBlocks);
		tableConfig.setFilter(filter);
		tableConfig.setPinTopLevelIndexAndFilter(pinTopLevelIndexAndFilter);
		options.setWriteBufferManager(writeBufferManager);
		options.setWriteBufferSize(memTableSize);
		options.setTableFormatConfig(tableConfig);
		options.setMaxWriteBufferNumber(maxWriteBufferNumber);
	}

	@Override
	public void close(final String storeName, final Options options) {
		filter.close();
		cache.close();
	}
}
