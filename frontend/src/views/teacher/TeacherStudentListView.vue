<template>
  <div class="h-screen flex flex-row bg-background-light dark:bg-background-dark overflow-hidden">
    <!-- Sidebar - Hidden on mobile, shown on lg+ -->
    <TeacherSidebar class="hidden lg:flex h-full self-stretch" />
    
    <!-- Main Content -->
    <main class="flex-1 flex flex-col min-w-0 overflow-hidden">
      <!-- Header -->
      <header class="px-4 sm:px-6 lg:px-8 py-4 sm:py-6 border-b border-slate-200 dark:border-slate-800">
        <h1 class="text-xl sm:text-2xl font-bold text-slate-900 dark:text-white">学生管理</h1>
        <p class="text-slate-500 dark:text-slate-400 mt-1 text-sm sm:text-base">管理您班级的学生，查看学习数据和问答历史</p>
      </header>

      <!-- Content Area -->
      <div class="flex-1 overflow-auto p-4 sm:p-6 lg:p-8">
        <div class="space-y-4 sm:space-y-6">
          <!-- Stats Cards Row - Responsive Grid -->
          <div class="grid grid-cols-2 sm:grid-cols-4 gap-3 sm:gap-4">
            <!-- Stats Card - Total Students -->
            <div class="bg-white dark:bg-[#1a222c] rounded-lg p-3 sm:p-4 border border-slate-200 dark:border-slate-800">
              <template v-if="teacherStudentStore.loading && !teacherStudentStore.hasStudents">
                <!-- Skeleton Loading -->
                <div class="animate-pulse">
                  <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-16 mb-2"></div>
                  <div class="h-7 bg-slate-200 dark:bg-slate-700 rounded w-12"></div>
                </div>
              </template>
              <template v-else>
                <p class="text-slate-500 dark:text-slate-400 text-xs sm:text-sm">班级学生数</p>
                <p class="text-xl sm:text-2xl font-bold text-primary mt-1">{{ teacherStudentStore.totalStudents }}</p>
              </template>
            </div>
            <!-- Stats Card - Active Today (placeholder for future) -->
            <div class="bg-white dark:bg-[#1a222c] rounded-lg p-3 sm:p-4 border border-slate-200 dark:border-slate-800">
              <template v-if="teacherStudentStore.loading && !teacherStudentStore.hasStudents">
                <div class="animate-pulse">
                  <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-16 mb-2"></div>
                  <div class="h-7 bg-slate-200 dark:bg-slate-700 rounded w-12"></div>
                </div>
              </template>
              <template v-else>
                <p class="text-slate-500 dark:text-slate-400 text-xs sm:text-sm">今日活跃</p>
                <p class="text-xl sm:text-2xl font-bold text-green-500 mt-1">{{ teacherStudentStore.activeToday || 0 }}</p>
              </template>
            </div>
            <!-- Stats Card - Total Questions -->
            <div class="bg-white dark:bg-[#1a222c] rounded-lg p-3 sm:p-4 border border-slate-200 dark:border-slate-800">
              <template v-if="teacherStudentStore.loading && !teacherStudentStore.hasStudents">
                <div class="animate-pulse">
                  <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-16 mb-2"></div>
                  <div class="h-7 bg-slate-200 dark:bg-slate-700 rounded w-12"></div>
                </div>
              </template>
              <template v-else>
                <p class="text-slate-500 dark:text-slate-400 text-xs sm:text-sm">总提问数</p>
                <p class="text-xl sm:text-2xl font-bold text-blue-500 mt-1">{{ teacherStudentStore.totalQuestions || 0 }}</p>
              </template>
            </div>
            <!-- Stats Card - This Week -->
            <div class="bg-white dark:bg-[#1a222c] rounded-lg p-3 sm:p-4 border border-slate-200 dark:border-slate-800">
              <template v-if="teacherStudentStore.loading && !teacherStudentStore.hasStudents">
                <div class="animate-pulse">
                  <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-16 mb-2"></div>
                  <div class="h-7 bg-slate-200 dark:bg-slate-700 rounded w-12"></div>
                </div>
              </template>
              <template v-else>
                <p class="text-slate-500 dark:text-slate-400 text-xs sm:text-sm">本周新增</p>
                <p class="text-xl sm:text-2xl font-bold text-orange-500 mt-1">{{ teacherStudentStore.newThisWeek || 0 }}</p>
              </template>
            </div>
          </div>

          <!-- Search and Action Buttons Row - Responsive -->
          <div class="flex flex-col sm:flex-row sm:items-center gap-3 sm:gap-4">
            <!-- Search Input -->
            <div class="flex-1 min-w-0 sm:max-w-md">
              <div class="relative">
                <span class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-slate-400">
                  search
                </span>
                <input
                  v-model="searchQuery"
                  type="text"
                  placeholder="搜索用户名或真实姓名..."
                  class="w-full h-10 sm:h-11 pl-12 pr-4 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-[#1a222c] text-slate-900 dark:text-white placeholder:text-slate-400 focus:outline-none focus:border-primary transition-all text-sm sm:text-base"
                  @input="handleSearch"
                />
              </div>
            </div>

            <!-- Action Buttons - Responsive -->
            <div class="flex items-center gap-2 sm:gap-3 flex-wrap sm:flex-nowrap">
              <!-- Refresh Button -->
              <button
                @click="handleRefresh"
                :disabled="teacherStudentStore.loading"
                class="h-9 px-3 sm:px-4 rounded-lg bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400 text-sm font-medium hover:bg-slate-200 dark:hover:bg-slate-700 transition-all flex items-center gap-1 sm:gap-2 disabled:opacity-50"
              >
                <span 
                  class="material-symbols-outlined text-lg"
                  :class="{ 'animate-spin': teacherStudentStore.loading }"
                >
                  refresh
                </span>
                <span class="hidden sm:inline">刷新</span>
              </button>

              <!-- Add Student Button -->
              <button
                @click="showAddStudentModal = true"
                class="h-9 px-3 sm:px-4 rounded-lg bg-primary text-white text-sm font-medium hover:bg-blue-600 transition-all flex items-center gap-1 sm:gap-2"
              >
                <span class="material-symbols-outlined text-lg">person_add</span>
                <span class="hidden xs:inline">添加学生</span>
              </button>

              <!-- Export Button -->
              <button
                @click="showExportModal = true"
                class="h-9 px-3 sm:px-4 rounded-lg bg-green-500 text-white text-sm font-medium hover:bg-green-600 transition-all flex items-center gap-1 sm:gap-2"
              >
                <span class="material-symbols-outlined text-lg">download</span>
                <span class="hidden xs:inline">导出报表</span>
              </button>
            </div>
          </div>

          <!-- Students Table - Desktop View -->
          <div class="bg-white dark:bg-[#1a222c] rounded-lg border border-slate-200 dark:border-slate-800 overflow-hidden">
            <!-- Skeleton Loading State -->
            <div v-if="teacherStudentStore.loading && !teacherStudentStore.hasStudents" class="p-4 sm:p-6">
              <!-- Skeleton Table Header -->
              <div class="hidden sm:grid grid-cols-6 gap-4 pb-4 border-b border-slate-200 dark:border-slate-700">
                <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded animate-pulse"></div>
                <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded animate-pulse"></div>
                <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded animate-pulse"></div>
                <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded animate-pulse"></div>
                <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded animate-pulse"></div>
                <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded animate-pulse"></div>
              </div>
              <!-- Skeleton Rows -->
              <div v-for="i in 5" :key="i" class="py-4 border-b border-slate-200 dark:border-slate-700 last:border-b-0">
                <div class="hidden sm:grid grid-cols-6 gap-4 items-center">
                  <div class="flex items-center gap-3">
                    <div class="w-9 h-9 rounded-full bg-slate-200 dark:bg-slate-700 animate-pulse"></div>
                    <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-20 animate-pulse"></div>
                  </div>
                  <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-16 animate-pulse"></div>
                  <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-32 animate-pulse"></div>
                  <div class="h-6 bg-slate-200 dark:bg-slate-700 rounded-full w-10 animate-pulse"></div>
                  <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-28 animate-pulse"></div>
                  <div class="flex gap-2">
                    <div class="w-8 h-8 bg-slate-200 dark:bg-slate-700 rounded animate-pulse"></div>
                    <div class="w-8 h-8 bg-slate-200 dark:bg-slate-700 rounded animate-pulse"></div>
                  </div>
                </div>
                <!-- Mobile Skeleton -->
                <div class="sm:hidden space-y-3">
                  <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-full bg-slate-200 dark:bg-slate-700 animate-pulse"></div>
                    <div class="flex-1 space-y-2">
                      <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-24 animate-pulse"></div>
                      <div class="h-3 bg-slate-200 dark:bg-slate-700 rounded w-32 animate-pulse"></div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Desktop Table View -->
            <div v-else class="hidden sm:block overflow-x-auto">
              <table class="w-full min-w-[700px]">
                <thead>
                  <tr class="bg-slate-50 dark:bg-slate-800/50 border-b border-slate-200 dark:border-slate-700">
                    <th class="px-4 lg:px-6 py-3 lg:py-4 text-left text-xs lg:text-sm font-semibold text-slate-900 dark:text-white">用户名</th>
                    <th class="px-4 lg:px-6 py-3 lg:py-4 text-left text-xs lg:text-sm font-semibold text-slate-900 dark:text-white">真实姓名</th>
                    <th class="px-4 lg:px-6 py-3 lg:py-4 text-left text-xs lg:text-sm font-semibold text-slate-900 dark:text-white">邮箱</th>
                    <th class="px-4 lg:px-6 py-3 lg:py-4 text-left text-xs lg:text-sm font-semibold text-slate-900 dark:text-white">总提问数</th>
                    <th class="px-4 lg:px-6 py-3 lg:py-4 text-left text-xs lg:text-sm font-semibold text-slate-900 dark:text-white">最后活动时间</th>
                    <th class="px-4 lg:px-6 py-3 lg:py-4 text-left text-xs lg:text-sm font-semibold text-slate-900 dark:text-white">操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="student in teacherStudentStore.students"
                    :key="student.id"
                    class="border-b border-slate-200 dark:border-slate-700 last:border-b-0 hover:bg-slate-50 dark:hover:bg-slate-800/30 transition-colors"
                  >
                    <td class="px-4 lg:px-6 py-3 lg:py-4">
                      <div class="flex items-center gap-2 lg:gap-3">
                        <div class="w-8 h-8 lg:w-9 lg:h-9 rounded-full bg-slate-200 dark:bg-slate-700 flex items-center justify-center flex-shrink-0">
                          <span class="material-symbols-outlined text-slate-500 text-base lg:text-lg">person</span>
                        </div>
                        <span class="text-xs lg:text-sm font-medium text-slate-900 dark:text-white truncate max-w-[100px] lg:max-w-none">{{ student.username }}</span>
                      </div>
                    </td>
                    <td class="px-4 lg:px-6 py-3 lg:py-4 text-xs lg:text-sm text-slate-600 dark:text-slate-400">{{ student.realName || '-' }}</td>
                    <td class="px-4 lg:px-6 py-3 lg:py-4 text-xs lg:text-sm text-slate-600 dark:text-slate-400 truncate max-w-[150px]">{{ student.email }}</td>
                    <td class="px-4 lg:px-6 py-3 lg:py-4">
                      <span class="inline-flex items-center px-2 py-0.5 lg:px-2.5 lg:py-1 rounded-full text-xs font-medium bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400">
                        {{ student.totalQuestions || 0 }}
                      </span>
                    </td>
                    <td class="px-4 lg:px-6 py-3 lg:py-4 text-xs lg:text-sm text-slate-500 dark:text-slate-400 whitespace-nowrap">{{ formatDate(student.lastActivity) }}</td>
                    <td class="px-4 lg:px-6 py-3 lg:py-4">
                      <div class="flex items-center gap-1 lg:gap-2">
                        <button
                          @click="viewStudentDetail(student)"
                          class="p-1.5 lg:p-2 rounded-lg text-slate-500 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 hover:text-primary transition-all"
                          title="查看详情"
                        >
                          <span class="material-symbols-outlined text-base lg:text-lg">visibility</span>
                        </button>
                        <button
                          @click="confirmRemoveStudent(student)"
                          class="p-1.5 lg:p-2 rounded-lg text-slate-500 dark:text-slate-400 hover:bg-red-50 dark:hover:bg-red-900/20 hover:text-red-500 transition-all"
                          title="移除学生"
                        >
                          <span class="material-symbols-outlined text-base lg:text-lg">person_remove</span>
                        </button>
                      </div>
                    </td>
                  </tr>
                  <!-- Empty State -->
                  <tr v-if="!teacherStudentStore.loading && teacherStudentStore.students.length === 0">
                    <td colspan="6" class="px-6 py-12 text-center">
                      <div class="flex flex-col items-center gap-3">
                        <span class="material-symbols-outlined text-4xl text-slate-300 dark:text-slate-600">group_off</span>
                        <p class="text-slate-500 dark:text-slate-400">
                          {{ searchQuery ? '未找到匹配的学生' : '暂无学生，点击"添加学生"开始' }}
                        </p>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- Mobile Card View -->
            <div v-if="!teacherStudentStore.loading || teacherStudentStore.hasStudents" class="sm:hidden divide-y divide-slate-200 dark:divide-slate-700">
              <div
                v-for="student in teacherStudentStore.students"
                :key="student.id"
                class="p-4 hover:bg-slate-50 dark:hover:bg-slate-800/30 transition-colors"
              >
                <div class="flex items-start gap-3">
                  <div class="w-10 h-10 rounded-full bg-slate-200 dark:bg-slate-700 flex items-center justify-center flex-shrink-0">
                    <span class="material-symbols-outlined text-slate-500">person</span>
                  </div>
                  <div class="flex-1 min-w-0">
                    <div class="flex items-center justify-between gap-2">
                      <h4 class="text-sm font-medium text-slate-900 dark:text-white truncate">{{ student.username }}</h4>
                      <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400 flex-shrink-0">
                        {{ student.totalQuestions || 0 }} 问
                      </span>
                    </div>
                    <p class="text-xs text-slate-500 dark:text-slate-400 mt-0.5">{{ student.realName || '-' }}</p>
                    <p class="text-xs text-slate-400 dark:text-slate-500 truncate mt-1">{{ student.email }}</p>
                    <p class="text-xs text-slate-400 dark:text-slate-500 mt-1">最后活动: {{ formatDate(student.lastActivity) }}</p>
                  </div>
                </div>
                <div class="flex items-center justify-end gap-2 mt-3 pt-3 border-t border-slate-100 dark:border-slate-800">
                  <button
                    @click="viewStudentDetail(student)"
                    class="flex-1 h-8 px-3 rounded-lg bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400 text-xs font-medium hover:bg-slate-200 dark:hover:bg-slate-700 transition-all flex items-center justify-center gap-1"
                  >
                    <span class="material-symbols-outlined text-sm">visibility</span>
                    查看详情
                  </button>
                  <button
                    @click="confirmRemoveStudent(student)"
                    class="h-8 px-3 rounded-lg text-red-500 hover:bg-red-50 dark:hover:bg-red-900/20 text-xs font-medium transition-all flex items-center justify-center gap-1"
                  >
                    <span class="material-symbols-outlined text-sm">person_remove</span>
                    移除
                  </button>
                </div>
              </div>
              <!-- Mobile Empty State -->
              <div v-if="!teacherStudentStore.loading && teacherStudentStore.students.length === 0" class="p-8 text-center">
                <div class="flex flex-col items-center gap-3">
                  <span class="material-symbols-outlined text-4xl text-slate-300 dark:text-slate-600">group_off</span>
                  <p class="text-slate-500 dark:text-slate-400 text-sm">
                    {{ searchQuery ? '未找到匹配的学生' : '暂无学生，点击"添加学生"开始' }}
                  </p>
                </div>
              </div>
            </div>

            <!-- Pagination - Responsive -->
            <div 
              v-if="teacherStudentStore.pagination.totalPages > 1"
              class="px-4 sm:px-6 py-3 sm:py-4 border-t border-slate-200 dark:border-slate-700 flex flex-col sm:flex-row items-center justify-between gap-3"
            >
              <p class="text-xs sm:text-sm text-slate-500 dark:text-slate-400 text-center sm:text-left">
                共 {{ teacherStudentStore.pagination.totalElements }} 名学生，
                第 {{ teacherStudentStore.pagination.page + 1 }} / {{ teacherStudentStore.pagination.totalPages }} 页
              </p>
              <div class="flex items-center gap-2">
                <button
                  @click="goToPage(teacherStudentStore.pagination.page - 1)"
                  :disabled="teacherStudentStore.isFirstPage"
                  class="h-8 sm:h-9 px-2 sm:px-3 rounded-lg border border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-400 text-xs sm:text-sm font-medium hover:bg-slate-100 dark:hover:bg-slate-800 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-1"
                >
                  <span class="material-symbols-outlined text-base sm:text-lg">chevron_left</span>
                  <span class="hidden xs:inline">上一页</span>
                </button>
                <button
                  @click="goToPage(teacherStudentStore.pagination.page + 1)"
                  :disabled="teacherStudentStore.isLastPage"
                  class="h-8 sm:h-9 px-2 sm:px-3 rounded-lg border border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-400 text-xs sm:text-sm font-medium hover:bg-slate-100 dark:hover:bg-slate-800 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-1"
                >
                  <span class="hidden xs:inline">下一页</span>
                  <span class="material-symbols-outlined text-base sm:text-lg">chevron_right</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Add Student Modal -->
    <AddStudentModal
      v-if="showAddStudentModal"
      @close="showAddStudentModal = false"
      @success="handleAddStudentSuccess"
    />

    <!-- Export Report Modal -->
    <ExportReportModal
      v-if="showExportModal"
      @close="showExportModal = false"
      @success="handleExportSuccess"
    />

    <!-- Remove Student Confirmation Modal -->
    <div
      v-if="removingStudent"
      class="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4"
      @click="removingStudent = null"
    >
      <div class="bg-white dark:bg-[#1a222c] rounded-lg p-4 sm:p-6 max-w-md w-full" @click.stop>
        <div class="flex items-center gap-3 mb-4">
          <div class="w-10 h-10 rounded-full bg-red-100 dark:bg-red-900/30 flex items-center justify-center flex-shrink-0">
            <span class="material-symbols-outlined text-red-600">warning</span>
          </div>
          <h3 class="text-base sm:text-lg font-semibold text-slate-900 dark:text-white">确认移除</h3>
        </div>
        <p class="text-sm sm:text-base text-slate-600 dark:text-slate-400 mb-4 sm:mb-6">
          确定要将学生 <span class="font-semibold text-slate-900 dark:text-white">{{ removingStudent.realName || removingStudent.username }}</span> 从您的班级中移除吗？
        </p>
        <p class="text-xs sm:text-sm text-slate-500 bg-slate-50 dark:bg-slate-800/50 rounded-lg p-3 mb-4 sm:mb-6">
          <span class="material-symbols-outlined text-sm align-middle mr-1">info</span>
          此操作不会删除学生账号，仅解除关联关系。
        </p>
        <div class="flex flex-col-reverse sm:flex-row justify-end gap-2 sm:gap-3">
          <button
            @click="removingStudent = null"
            class="w-full sm:w-auto px-4 py-2 rounded-lg text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 transition-all text-sm sm:text-base"
          >
            取消
          </button>
          <button
            @click="handleRemoveStudent"
            :disabled="teacherStudentStore.loading"
            class="w-full sm:w-auto px-4 py-2 rounded-lg bg-red-500 text-white hover:bg-red-600 transition-all disabled:opacity-50 flex items-center justify-center gap-2 text-sm sm:text-base"
          >
            <span v-if="teacherStudentStore.loading" class="material-symbols-outlined text-lg animate-spin">progress_activity</span>
            {{ teacherStudentStore.loading ? '移除中...' : '确认移除' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { useTeacherStudentStore } from '../../stores/teacherStudent'
import { useToast } from '@/composables/useToast'
import TeacherSidebar from '../../components/teacher/TeacherSidebar.vue'
import AddStudentModal from '../../components/teacher/AddStudentModal.vue'
import ExportReportModal from '../../components/teacher/ExportReportModal.vue'

const router = useRouter()
const userStore = useUserStore()
const teacherStudentStore = useTeacherStudentStore()
const toast = useToast()

// Search
const searchQuery = ref('')
let searchTimeout = null

// Modals
const showAddStudentModal = ref(false)
const showExportModal = ref(false)
const removingStudent = ref(null)

// Check teacher access and load data
onMounted(async () => {
  if (userStore.user.role !== 'TEACHER') {
    router.push('/')
    return
  }
  await teacherStudentStore.fetchStudents()
})

// Search handler with debounce
function handleSearch() {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    teacherStudentStore.fetchStudents(0, searchQuery.value)
  }, 300)
}

// Refresh student list
async function handleRefresh() {
  await teacherStudentStore.fetchStudents(teacherStudentStore.pagination.page, searchQuery.value)
  toast.success('刷新成功')
}

// Navigate to student detail page
function viewStudentDetail(student) {
  router.push(`/teacher/students/${student.id}`)
}

// Confirm remove student
function confirmRemoveStudent(student) {
  removingStudent.value = student
}

// Handle remove student
async function handleRemoveStudent() {
  if (!removingStudent.value) return
  
  const result = await teacherStudentStore.removeStudent(removingStudent.value.id)
  
  if (result.success) {
    toast.success('学生移除成功')
  } else {
    toast.error(result.message || '移除学生失败')
  }
  
  removingStudent.value = null
}

// Handle add student success
function handleAddStudentSuccess(message) {
  toast.success(message || '学生添加成功')
}

// Handle export success
function handleExportSuccess() {
  toast.success('报表导出成功')
}

// Go to specific page
function goToPage(page) {
  if (page >= 0 && page < teacherStudentStore.pagination.totalPages) {
    teacherStudentStore.fetchStudents(page, searchQuery.value)
  }
}

// Format date
function formatDate(dateString) {
  if (!dateString) return '-'
  try {
    const date = new Date(dateString)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return dateString
  }
}
</script>
