<script setup>
defineProps({
  currentPage: {
    type: Number,
    required: true,
  },
  totalPages: {
    type: Number,
    required: true,
  },
  totalElements: {
    type: Number,
    required: true,
  },
  pageSize: {
    type: Number,
    default: 10,
  },
})

const emit = defineEmits(['page-change', 'page-size-change'])

const pageSizes = [5, 10, 20, 50]

function goToPage(page) {
  emit('page-change', page)
}

function changePageSize(event) {
  emit('page-size-change', parseInt(event.target.value, 10))
}

function getVisiblePages(current, total) {
  const pages = []
  const delta = 2

  if (total <= 7) {
    for (let i = 0; i < total; i++) {
      pages.push(i)
    }
    return pages
  }

  pages.push(0)

  if (current > delta + 1) {
    pages.push(-1) // ellipsis
  }

  const start = Math.max(1, current - delta)
  const end = Math.min(total - 2, current + delta)

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }

  if (current < total - delta - 2) {
    pages.push(-1) // ellipsis
  }

  pages.push(total - 1)

  return pages
}
</script>

<template>
  <div class="pagination">
    <div class="pagination__info">
      <span class="pagination__total">
        Total: {{ totalElements }} items
      </span>
    </div>

    <div class="pagination__controls">
      <button
        type="button"
        class="pagination__btn pagination__btn--nav"
        :disabled="currentPage === 0"
        @click="goToPage(0)"
        title="First page"
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="11 17 6 12 11 7"></polyline>
          <polyline points="18 17 13 12 18 7"></polyline>
        </svg>
      </button>

      <button
        type="button"
        class="pagination__btn pagination__btn--nav"
        :disabled="currentPage === 0"
        @click="goToPage(currentPage - 1)"
        title="Previous page"
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="15 18 9 12 15 6"></polyline>
        </svg>
      </button>

      <span class="pagination__pages">
        <template v-for="page in getVisiblePages(currentPage, totalPages)" :key="page">
          <span v-if="page === -1" class="pagination__ellipsis">...</span>
          <button
            v-else
            type="button"
            class="pagination__btn pagination__btn--page"
            :class="{ 'pagination__btn--active': page === currentPage }"
            @click="goToPage(page)"
          >
            {{ page + 1 }}
          </button>
        </template>
      </span>

      <button
        type="button"
        class="pagination__btn pagination__btn--nav"
        :disabled="currentPage >= totalPages - 1"
        @click="goToPage(currentPage + 1)"
        title="Next page"
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="9 18 15 12 9 6"></polyline>
        </svg>
      </button>

      <button
        type="button"
        class="pagination__btn pagination__btn--nav"
        :disabled="currentPage >= totalPages - 1"
        @click="goToPage(totalPages - 1)"
        title="Last page"
      >
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="13 17 18 12 13 7"></polyline>
          <polyline points="6 17 11 12 6 7"></polyline>
        </svg>
      </button>

      <select
        class="pagination__select"
        :value="pageSize"
        @change="changePageSize"
      >
        <option v-for="size in pageSizes" :key="size" :value="size">
          {{ size }} / page
        </option>
      </select>
    </div>
  </div>
</template>

<style scoped>
.pagination {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  margin-top: 1rem;
}

.pagination__info {
  font-size: 0.875rem;
  color: #6b7d6b;
}

.pagination__controls {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.pagination__btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 36px;
  height: 36px;
  padding: 0 0.5rem;
  border: 1px solid #d4e4d4;
  border-radius: 6px;
  background: white;
  color: #5a6b5a;
  font-size: 0.875rem;
  cursor: pointer;
  transition: all 0.15s ease;
}

.pagination__btn:hover:not(:disabled) {
  background: #f0f7f0;
  border-color: #34c759;
  color: #2d8a3e;
}

.pagination__btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.pagination__btn--active {
  background: #34c759;
  border-color: #34c759;
  color: white;
}

.pagination__btn--active:hover:not(:disabled) {
  background: #2d8a3e;
  border-color: #2d8a3e;
  color: white;
}

.pagination__pages {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.pagination__ellipsis {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 36px;
  height: 36px;
  color: #6b7d6b;
  font-size: 0.875rem;
}

.pagination__select {
  margin-left: 0.75rem;
  padding: 0.5rem;
  border: 1px solid #d4e4d4;
  border-radius: 6px;
  background: white;
  color: #5a6b5a;
  font-size: 0.875rem;
  cursor: pointer;
}

.pagination__select:hover {
  border-color: #34c759;
}

@media (max-width: 640px) {
  .pagination {
    padding: 0.75rem;
  }

  .pagination__controls {
    flex-wrap: wrap;
    justify-content: center;
  }

  .pagination__select {
    margin-left: 0;
    margin-top: 0.5rem;
    width: 100%;
  }
}
</style>
