// Copyright Square, Inc.
package app.cash.sqldelight.paging3

import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.room.RoomDatabase
import app.cash.sqldelight.Query
import app.cash.sqldelight.paging3.util.INITIAL_ITEM_COUNT
import app.cash.sqldelight.paging3.util.INVALID
import app.cash.sqldelight.paging3.util.ThreadSafeInvalidationObserver
import app.cash.sqldelight.paging3.util.getClippedRefreshKey
import app.cash.sqldelight.paging3.util.queryDatabase
import app.cash.sqldelight.Transacter
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext

/**
 * An implementation of [PagingSource] to perform a LIMIT OFFSET query
 *
 * This class is used for Paging3 to perform Query and RawQuery in Room to return a PagingSource
 * for Pager's consumption. Registers observers on tables lazily and automatically invalidates
 * itself when data changes.
 */
abstract class LimitOffsetPagingSource<RowType : Any>(
  private val queryProvider: (limit: Int, offset: Int) -> Query<RowType>,
  private val countQuery: Query<Int>,
  private val transacter: Transacter,
  private val context: CoroutineContext,
  private val db: RoomDatabase,
  vararg tables: String,
) : PagingSource<Int, RowType>() {

  internal val itemCount: AtomicInteger = AtomicInteger(INITIAL_ITEM_COUNT)

  private val observer = ThreadSafeInvalidationObserver(
    tables = tables,
    onInvalidated = ::invalidate
  )

  override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RowType> {
    return withContext(context) {
      observer.registerIfNecessary(db)
      val tempCount = itemCount.get()
      // if itemCount is < 0, then it is initial load
      if (tempCount == INITIAL_ITEM_COUNT) {
        initialLoad(params)
      } else {
        nonInitialLoad(params, tempCount)
      }
    }
  }

  /**
   *  For the very first time that this PagingSource's [load] is called. Executes the count
   *  query (initializes [itemCount]) and db query within a transaction to ensure initial load's
   *  data integrity.
   *
   *  For example, if the database gets updated after the count query but before the db query
   *  completes, the paging source may not invalidate in time, but this method will return
   *  data based on the original database that the count was performed on to ensure a valid
   *  initial load.
   */
  private suspend fun initialLoad(params: LoadParams<Int>): LoadResult<Int, RowType> {
    return transacter.transactionWithResult {
      val tempCount = countQuery.executeAsOne()
      itemCount.set(tempCount)
      queryDatabase(
        queryProvider = queryProvider,
        params = params,
        itemCount = tempCount,
      )
    }
  }

  private suspend fun nonInitialLoad(
    params: LoadParams<Int>,
    tempCount: Int,
  ): LoadResult<Int, RowType> {
    val loadResult = queryDatabase(
      queryProvider = queryProvider,
      params = params,
      itemCount = tempCount,
    )
    // manually check if database has been updated. If so, the observer's
    // invalidation callback will invalidate this paging source
    db.invalidationTracker.refreshVersionsSync()
    @Suppress("UNCHECKED_CAST")
    return if (invalid) INVALID as LoadResult.Invalid<Int, RowType> else loadResult
  }

  override fun getRefreshKey(state: PagingState<Int, RowType>): Int? {
    return state.getClippedRefreshKey()
  }

  override val jumpingSupported: Boolean
    get() = true
}
