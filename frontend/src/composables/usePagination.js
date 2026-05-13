import { ref, computed } from 'vue'

const DEFAULT_PAGE_SIZE = 10

export function usePagination(initialPageSize = DEFAULT_PAGE_SIZE) {
  const currentPage = ref(0)
  const pageSize = ref(initialPageSize)
  const totalElements = ref(0)

  const totalPages = computed(() => {
    if (totalElements.value === 0) return 0
    return Math.ceil(totalElements.value / pageSize.value)
  })

  const hasNextPage = computed(() => currentPage.value < totalPages.value - 1)
  const hasPreviousPage = computed(() => currentPage.value > 0)
  const isFirstPage = computed(() => currentPage.value === 0)
  const isLastPage = computed(() => currentPage.value >= totalPages.value - 1)

  function setPage(page) {
    currentPage.value = Math.max(0, Math.min(page, totalPages.value - 1))
  }

  function nextPage() {
    if (hasNextPage.value) {
      currentPage.value++
    }
  }

  function previousPage() {
    if (hasPreviousPage.value) {
      currentPage.value--
    }
  }

  function firstPage() {
    currentPage.value = 0
  }

  function lastPage() {
    currentPage.value = Math.max(0, totalPages.value - 1)
  }

  function updateTotalElements(total) {
    totalElements.value = total
  }

  function updatePageInfo(pageInfo) {
    if (pageInfo.page !== undefined) currentPage.value = pageInfo.page
    if (pageInfo.size !== undefined) pageSize.value = pageInfo.size
    if (pageInfo.totalElements !== undefined) totalElements.value = pageInfo.totalElements
  }

  function reset() {
    currentPage.value = 0
    totalElements.value = 0
  }

  function getPaginationParams() {
    return {
      page: currentPage.value,
      size: pageSize.value,
    }
  }

  return {
    currentPage,
    pageSize,
    totalElements,
    totalPages,
    hasNextPage,
    hasPreviousPage,
    isFirstPage,
    isLastPage,
    setPage,
    nextPage,
    previousPage,
    firstPage,
    lastPage,
    updateTotalElements,
    updatePageInfo,
    reset,
    getPaginationParams,
  }
}
