<script setup>
defineProps({
  /** 类型: table-row, card, text, avatar, button */
  type: {
    type: String,
    default: 'text',
  },
  /** 行数 (用于 table-row 和 text 类型) */
  rows: {
    type: Number,
    default: 5,
  },
  /** 高度 */
  height: {
    type: String,
    default: '1rem',
  },
  /** 宽度 */
  width: {
    type: String,
    default: '100%',
  },
  /** 圆角 */
  borderRadius: {
    type: String,
    default: '4px',
  },
})
</script>

<template>
  <!-- 表格行骨架屏 -->
  <template v-if="type === 'table-row'">
    <div v-for="i in rows" :key="i" class="skeleton-table-row">
      <div class="skeleton-table-cell skeleton-cell--index"></div>
      <div class="skeleton-table-cell skeleton-cell--sm"></div>
      <div class="skeleton-table-cell"></div>
      <div class="skeleton-table-cell"></div>
      <div class="skeleton-table-cell"></div>
      <div class="skeleton-table-cell skeleton-cell--lg"></div>
      <div class="skeleton-table-cell skeleton-cell--sm"></div>
      <div class="skeleton-table-cell skeleton-cell--sm"></div>
      <div class="skeleton-table-cell skeleton-cell--md"></div>
    </div>
  </template>

  <!-- 卡片骨架屏 -->
  <template v-else-if="type === 'card'">
    <div v-for="i in rows" :key="i" class="skeleton-card">
      <div class="skeleton-card-header">
        <div class="skeleton-avatar"></div>
        <div class="skeleton-card-title">
          <div class="skeleton-line skeleton-line--short"></div>
          <div class="skeleton-line skeleton-line--medium"></div>
        </div>
      </div>
      <div class="skeleton-card-body">
        <div class="skeleton-line"></div>
        <div class="skeleton-line"></div>
        <div class="skeleton-line skeleton-line--short"></div>
      </div>
    </div>
  </template>

  <!-- 列表骨架屏 -->
  <template v-else-if="type === 'list'">
    <div v-for="i in rows" :key="i" class="skeleton-list-item">
      <div class="skeleton-avatar skeleton-avatar--sm"></div>
      <div class="skeleton-list-content">
        <div class="skeleton-line skeleton-line--medium"></div>
        <div class="skeleton-line skeleton-line--short"></div>
      </div>
    </div>
  </template>

  <!-- 统计卡片骨架屏 -->
  <template v-else-if="type === 'stat-card'">
    <div v-for="i in rows" :key="i" class="skeleton-stat-card">
      <div class="skeleton-stat-icon"></div>
      <div class="skeleton-stat-content">
        <div class="skeleton-line skeleton-line--short skeleton-line--light"></div>
        <div class="skeleton-line skeleton-line--large"></div>
      </div>
    </div>
  </template>

  <!-- 图表骨架屏 -->
  <template v-else-if="type === 'chart'">
    <div class="skeleton-chart">
      <div class="skeleton-chart-header">
        <div class="skeleton-line skeleton-line--short"></div>
        <div class="skeleton-line skeleton-line--medium"></div>
      </div>
      <div class="skeleton-chart-body">
        <div class="skeleton-chart-bars">
          <div v-for="i in 7" :key="i" class="skeleton-chart-bar" :style="{ height: Math.random() * 60 + 40 + '%' }"></div>
        </div>
      </div>
    </div>
  </template>

  <!-- 通用文字行 -->
  <template v-else>
    <div
      class="skeleton-line"
      :style="{
        height,
        width,
        borderRadius,
      }"
    ></div>
  </template>
</template>

<style scoped>
/* 基础骨架动画 */
.skeleton-line,
.skeleton-avatar,
.skeleton-cell,
.skeleton-chart-bar,
.skeleton-stat-icon {
  background: linear-gradient(
    90deg,
    #e8efe8 0%,
    #d4e4d4 40%,
    #e8efe8 80%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s ease-in-out infinite;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* 通用文字行 */
.skeleton-line {
  height: 1rem;
  border-radius: 4px;
  margin-bottom: 0.5rem;
}

.skeleton-line:last-child {
  margin-bottom: 0;
}

.skeleton-line--short {
  width: 40%;
}

.skeleton-line--medium {
  width: 70%;
}

.skeleton-line--large {
  width: 85%;
  height: 1.5rem;
}

.skeleton-line--light {
  opacity: 0.6;
}

/* 头像 */
.skeleton-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
}

.skeleton-avatar--sm {
  width: 32px;
  height: 32px;
}

/* 表格行骨架屏 */
.skeleton-table-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.65rem 0.75rem;
  border-bottom: 1px solid #e0eae0;
}

.skeleton-table-cell {
  height: 1rem;
  border-radius: 4px;
  flex: 1;
}

.skeleton-cell--sm {
  flex: 0.5;
}

.skeleton-cell--md {
  flex: 0.8;
}

.skeleton-cell--lg {
  flex: 1.5;
}

.skeleton-cell--index {
  width: 30px;
  flex: none;
}

/* 卡片骨架屏 */
.skeleton-card {
  background: #fff;
  border: 1px solid #c5d6c5;
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 1rem;
}

.skeleton-card-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.skeleton-card-title {
  flex: 1;
}

.skeleton-card-body .skeleton-line:last-child {
  margin-bottom: 0;
}

/* 列表骨架屏 */
.skeleton-list-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 0;
  border-bottom: 1px solid #e0eae0;
}

.skeleton-list-content {
  flex: 1;
}

/* 统计卡片骨架屏 */
.skeleton-stat-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  background: #fff;
  border: 1px solid #c5d6c5;
  border-radius: 8px;
  padding: 1.25rem;
}

.skeleton-stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  flex-shrink: 0;
}

.skeleton-stat-content {
  flex: 1;
}

/* 图表骨架屏 */
.skeleton-chart {
  background: #fff;
  border: 1px solid #c5d6c5;
  border-radius: 8px;
  padding: 1rem;
}

.skeleton-chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.skeleton-chart-body {
  height: 200px;
  display: flex;
  align-items: flex-end;
}

.skeleton-chart-bars {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  width: 100%;
  height: 100%;
}

.skeleton-chart-bar {
  width: 12%;
  border-radius: 4px 4px 0 0;
}
</style>
