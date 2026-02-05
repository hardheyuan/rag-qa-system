<template>
  <div class="h-screen flex flex-row bg-background-light dark:bg-background-dark overflow-hidden">
    <!-- Sidebar - Hidden on mobile -->
    <TeacherSidebar class="hidden lg:flex h-full self-stretch" />
    
    <!-- Main Content -->
    <main class="flex-1 flex flex-col min-w-0 overflow-hidden">
      <!-- Header with Back Button - Responsive -->
      <header class="px-4 sm:px-6 lg:px-8 py-4 sm:py-6 border-b border-slate-200 dark:border-slate-800">
        <div class="flex items-center gap-3 sm:gap-4">
          <button
            @click="goBack"
            class="p-1.5 sm:p-2 rounded-lg text-slate-500 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 hover:text-primary transition-all flex-shrink-0"
            title="返回学生列表"
          >
            <span class="material-symbols-outlined text-lg sm:text-xl">arrow_back</span>
          </button>
          <div class="min-w-0">
            <h1 class="text-lg sm:text-xl lg:text-2xl font-bold text-slate-900 dark:text-white truncate">
              {{ currentStudent?.realName || currentStudent?.username || '学生详情' }}
            </h1>
            <p class="text-slate-500 dark:text-slate-400 mt-0.5 sm:mt-1 text-xs sm:text-sm lg:text-base">查看学生的学习数据和问答历史</p>
          </div>
        </div>
      </header>

      <!-- Content Area -->
      <div class="flex-1 overflow-auto p-4 sm:p-6 lg:p-8">
        <!-- Loading State with Skeleton -->
        <div v-if="loading && !currentStudent" class="space-y-4 sm:space-y-6">
          <!-- Skeleton Info Cards -->
          <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4 sm:gap-6">
            <div v-for="i in 3" :key="i" class="bg-white dark:bg-[#1a222c] rounded-lg border border-slate-200 dark:border-slate-800 p-4 sm:p-6">
              <div class="animate-pulse">
                <div class="flex items-center gap-3 mb-4">
                  <div class="w-10 h-10 sm:w-12 sm:h-12 rounded-full bg-slate-200 dark:bg-slate-700"></div>
                  <div class="h-5 bg-slate-200 dark:bg-slate-700 rounded w-24"></div>
                </div>
                <div class="space-y-3">
                  <div class="flex justify-between">
                    <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-16"></div>
                    <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-20"></div>
                  </div>
                  <div class="flex justify-between">
                    <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-20"></div>
                    <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-16"></div>
                  </div>
                  <div class="flex justify-between">
                    <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-14"></div>
                    <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-32"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <!-- Skeleton Tabs -->
          <div class="bg-white dark:bg-[#1a222c] rounded-lg border border-slate-200 dark:border-slate-800">
            <div class="flex border-b border-slate-200 dark:border-slate-700 p-4 gap-4">
              <div class="h-8 bg-slate-200 dark:bg-slate-700 rounded w-24 animate-pulse"></div>
              <div class="h-8 bg-slate-200 dark:bg-slate-700 rounded w-24 animate-pulse"></div>
            </div>
            <div class="p-6 space-y-4">
              <div v-for="i in 3" :key="i" class="h-24 bg-slate-100 dark:bg-slate-800/50 rounded-lg animate-pulse"></div>
            </div>
          </div>
        </div>

        <!-- Error State -->
        <div v-else-if="error && !currentStudent" class="flex items-center justify-center h-64">
          <div class="text-center px-4">
            <span class="material-symbols-outlined text-4xl text-red-400">error</span>
            <p class="text-slate-500 dark:text-slate-400 mt-3 text-sm sm:text-base">{{ error }}</p>
            <button
              @click="goBack"
              class="mt-4 px-4 py-2 rounded-lg bg-primary text-white hover:bg-blue-600 transition-all text-sm sm:text-base"
            >
              返回学生列表
            </button>
          </div>
        </div>

        <!-- Student Detail Content -->
        <div v-else-if="currentStudent" class="space-y-4 sm:space-y-6">
          <!-- Info Cards Row - Responsive Grid -->
          <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4 sm:gap-6">
            <!-- Basic Info Card - Responsive -->
            <div class="bg-white dark:bg-[#1a222c] rounded-lg border border-slate-200 dark:border-slate-800 p-4 sm:p-6">
              <div class="flex items-center gap-2 sm:gap-3 mb-3 sm:mb-4">
                <div class="w-10 h-10 sm:w-12 sm:h-12 rounded-full bg-primary/10 flex items-center justify-center flex-shrink-0">
                  <span class="material-symbols-outlined text-primary text-xl sm:text-2xl">person</span>
                </div>
                <h3 class="text-base sm:text-lg font-semibold text-slate-900 dark:text-white">基本信息</h3>
              </div>
              <div class="space-y-2 sm:space-y-3">
                <div class="flex justify-between items-center">
                  <span class="text-slate-500 dark:text-slate-400 text-xs sm:text-sm">用户名</span>
                  <span class="text-slate-900 dark:text-white text-xs sm:text-sm font-medium truncate ml-2 max-w-[150px]">{{ currentStudent.username }}</span>
                </div>
                <div class="flex justify-between items-center">
                  <span class="text-slate-500 dark:text-slate-400 text-xs sm:text-sm">真实姓名</span>
                  <span class="text-slate-900 dark:text-white text-xs sm:text-sm font-medium">{{ currentStudent.realName || '-' }}</span>
                </div>
                <div class="flex justify-between items-center">
                  <span class="text-slate-500 dark:text-slate-400 text-xs sm:text-sm">邮箱</span>
                  <span class="text-slate-900 dark:text-white text-xs sm:text-sm font-medium truncate ml-2 max-w-[150px]">{{ currentStudent.email }}</span>
                </div>
                <div class="flex justify-between items-center">
                  <span class="text-slate-500 dark:text-slate-400 text-xs sm:text-sm">注册时间</span>
                  <span class="text-slate-900 dark:text-white text-xs sm:text-sm font-medium">{{ formatDate(currentStudent.registeredAt) }}</span>
                </div>
                <div class="flex justify-between items-center">
                  <span class="text-slate-500 dark:text-slate-400 text-xs sm:text-sm">加入班级</span>
                  <span class="text-slate-900 dark:text-white text-xs sm:text-sm font-medium">{{ formatDate(currentStudent.enrolledAt) }}</span>
                </div>
              </div>
            </div>

            <!-- Stats Card - Responsive -->
            <div class="bg-white dark:bg-[#1a222c] rounded-lg border border-slate-200 dark:border-slate-800 p-4 sm:p-6">
              <div class="flex items-center gap-2 sm:gap-3 mb-3 sm:mb-4">
                <div class="w-10 h-10 sm:w-12 sm:h-12 rounded-full bg-green-500/10 flex items-center justify-center flex-shrink-0">
                  <span class="material-symbols-outlined text-green-500 text-xl sm:text-2xl">analytics</span>
                </div>
                <h3 class="text-base sm:text-lg font-semibold text-slate-900 dark:text-white">统计数据</h3>
              </div>
              <div class="grid grid-cols-2 gap-3 sm:gap-4">
                <div class="bg-slate-50 dark:bg-slate-800/50 rounded-lg p-3 sm:p-4 text-center">
                  <p class="text-xl sm:text-2xl font-bold text-primary">{{ currentStudent.totalQuestions || 0 }}</p>
                  <p class="text-slate-500 dark:text-slate-400 text-xs sm:text-sm mt-1">总提问数</p>
                </div>
                <div class="bg-slate-50 dark:bg-slate-800/50 rounded-lg p-3 sm:p-4 text-center">
                  <p class="text-xl sm:text-2xl font-bold text-green-500">{{ currentStudent.totalDocumentAccesses || 0 }}</p>
                  <p class="text-slate-500 dark:text-slate-400 text-xs sm:text-sm mt-1">文档访问数</p>
                </div>
              </div>
            </div>

            <!-- Recent Activity Card - Responsive -->
            <div class="bg-white dark:bg-[#1a222c] rounded-lg border border-slate-200 dark:border-slate-800 p-4 sm:p-6 md:col-span-2 xl:col-span-1">
              <div class="flex items-center gap-2 sm:gap-3 mb-3 sm:mb-4">
                <div class="w-10 h-10 sm:w-12 sm:h-12 rounded-full bg-orange-500/10 flex items-center justify-center flex-shrink-0">
                  <span class="material-symbols-outlined text-orange-500 text-xl sm:text-2xl">schedule</span>
                </div>
                <h3 class="text-base sm:text-lg font-semibold text-slate-900 dark:text-white">最近30天活动</h3>
              </div>
              <div class="space-y-2 sm:space-y-3">
                <div class="flex justify-between items-center">
                  <span class="text-slate-500 dark:text-slate-400 text-xs sm:text-sm">提问数</span>
                  <span class="text-slate-900 dark:text-white text-xs sm:text-sm font-medium">
                    {{ currentStudent.recentActivity?.questionsLast30Days || 0 }}
                  </span>
                </div>
                <div class="flex justify-between items-center">
                  <span class="text-slate-500 dark:text-slate-400 text-xs sm:text-sm">文档访问数</span>
                  <span class="text-slate-900 dark:text-white text-xs sm:text-sm font-medium">
                    {{ currentStudent.recentActivity?.documentAccessesLast30Days || 0 }}
                  </span>
                </div>
                <div class="flex justify-between items-center">
                  <span class="text-slate-500 dark:text-slate-400 text-xs sm:text-sm">最后提问</span>
                  <span class="text-slate-900 dark:text-white text-xs sm:text-sm font-medium">
                    {{ formatDate(currentStudent.recentActivity?.lastQuestionAt) }}
                  </span>
                </div>
                <div class="flex justify-between items-center">
                  <span class="text-slate-500 dark:text-slate-400 text-xs sm:text-sm">最后访问文档</span>
                  <span class="text-slate-900 dark:text-white text-xs sm:text-sm font-medium">
                    {{ formatDate(currentStudent.recentActivity?.lastDocumentAccessAt) }}
                  </span>
                </div>
              </div>
            </div>
          </div>

          <!-- Tabs Section - Responsive -->
          <div class="bg-white dark:bg-[#1a222c] rounded-lg border border-slate-200 dark:border-slate-800">
            <!-- Tab Headers - Responsive -->
            <div class="flex border-b border-slate-200 dark:border-slate-700 overflow-x-auto">
              <button
                @click="activeTab = 'qa'"
                :class="[
                  'px-4 sm:px-6 py-3 sm:py-4 text-xs sm:text-sm font-medium transition-all relative whitespace-nowrap flex-shrink-0',
                  activeTab === 'qa'
                    ? 'text-primary'
                    : 'text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
                ]"
              >
                <span class="flex items-center gap-1.5 sm:gap-2">
                  <span class="material-symbols-outlined text-base sm:text-lg">forum</span>
                  问答历史
                </span>
                <div
                  v-if="activeTab === 'qa'"
                  class="absolute bottom-0 left-0 right-0 h-0.5 bg-primary"
                ></div>
              </button>
              <button
                @click="activeTab = 'documents'"
                :class="[
                  'px-4 sm:px-6 py-3 sm:py-4 text-xs sm:text-sm font-medium transition-all relative whitespace-nowrap flex-shrink-0',
                  activeTab === 'documents'
                    ? 'text-primary'
                    : 'text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
                ]"
              >
                <span class="flex items-center gap-1.5 sm:gap-2">
                  <span class="material-symbols-outlined text-base sm:text-lg">description</span>
                  文档访问
                </span>
                <div
                  v-if="activeTab === 'documents'"
                  class="absolute bottom-0 left-0 right-0 h-0.5 bg-primary"
                ></div>
              </button>
            </div>

            <!-- Tab Content -->
            <div class="p-4 sm:p-6">
              <!-- QA History Tab -->
              <div v-if="activeTab === 'qa'">
                <!-- Loading State with Skeleton -->
                <div v-if="tabLoading" class="space-y-4">
                  <div v-for="i in 3" :key="i" class="border border-slate-200 dark:border-slate-700 rounded-lg p-4 animate-pulse">
                    <div class="flex items-start gap-3 mb-3">
                      <div class="w-8 h-8 rounded-full bg-slate-200 dark:bg-slate-700"></div>
                      <div class="flex-1 space-y-2">
                        <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-16"></div>
                        <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-full"></div>
                        <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-3/4"></div>
                      </div>
                    </div>
                    <div class="flex items-start gap-3 ml-11">
                      <div class="w-8 h-8 rounded-full bg-slate-200 dark:bg-slate-700"></div>
                      <div class="flex-1 space-y-2">
                        <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-16"></div>
                        <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-full"></div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Empty State -->
                <div v-else-if="qaHistory.length === 0" class="text-center py-8 sm:py-12">
                  <span class="material-symbols-outlined text-3xl sm:text-4xl text-slate-300 dark:text-slate-600">forum</span>
                  <p class="text-slate-500 dark:text-slate-400 mt-3 text-sm sm:text-base">暂无问答记录</p>
                </div>

                <!-- QA List - Responsive -->
                <div v-else class="space-y-3 sm:space-y-4">
                  <div
                    v-for="qa in qaHistory"
                    :key="qa.id"
                    class="border border-slate-200 dark:border-slate-700 rounded-lg p-3 sm:p-4"
                  >
                    <div class="flex items-start gap-2 sm:gap-3 mb-2 sm:mb-3">
                      <div class="w-7 h-7 sm:w-8 sm:h-8 rounded-full bg-blue-100 dark:bg-blue-900/30 flex items-center justify-center flex-shrink-0">
                        <span class="material-symbols-outlined text-blue-600 dark:text-blue-400 text-xs sm:text-sm">help</span>
                      </div>
                      <div class="flex-1 min-w-0">
                        <div class="flex items-center justify-between gap-2 mb-1">
                          <p class="text-xs sm:text-sm font-medium text-slate-900 dark:text-white">问题</p>
                          <span class="text-[10px] sm:text-xs text-slate-400 flex-shrink-0">{{ formatDate(qa.askedAt) }}</span>
                        </div>
                        <p class="text-xs sm:text-sm text-slate-600 dark:text-slate-400 whitespace-pre-wrap break-words">{{ qa.question }}</p>
                      </div>
                    </div>
                    <div class="flex items-start gap-2 sm:gap-3 ml-9 sm:ml-11">
                      <div class="w-7 h-7 sm:w-8 sm:h-8 rounded-full bg-green-100 dark:bg-green-900/30 flex items-center justify-center flex-shrink-0">
                        <span class="material-symbols-outlined text-green-600 dark:text-green-400 text-xs sm:text-sm">smart_toy</span>
                      </div>
                      <div class="flex-1 min-w-0">
                        <p class="text-xs sm:text-sm font-medium text-slate-900 dark:text-white mb-1">回答</p>
                        <p class="text-xs sm:text-sm text-slate-600 dark:text-slate-400 whitespace-pre-wrap break-words">{{ qa.answer }}</p>
                      </div>
                    </div>
                  </div>

                  <!-- QA Pagination - Responsive -->
                  <div
                    v-if="qaHistoryPagination.totalPages > 1"
                    class="flex flex-col sm:flex-row items-center justify-between gap-3 pt-4 border-t border-slate-200 dark:border-slate-700"
                  >
                    <p class="text-xs sm:text-sm text-slate-500 dark:text-slate-400 text-center sm:text-left">
                      共 {{ qaHistoryPagination.totalElements }} 条记录，
                      第 {{ qaHistoryPagination.page + 1 }} / {{ qaHistoryPagination.totalPages }} 页
                    </p>
                    <div class="flex items-center gap-2">
                      <button
                        @click="loadQaHistory(qaHistoryPagination.page - 1)"
                        :disabled="qaHistoryPagination.page === 0"
                        class="h-8 sm:h-9 px-2 sm:px-3 rounded-lg border border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-400 text-xs sm:text-sm font-medium hover:bg-slate-100 dark:hover:bg-slate-800 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-1"
                      >
                        <span class="material-symbols-outlined text-base sm:text-lg">chevron_left</span>
                        <span class="hidden xs:inline">上一页</span>
                      </button>
                      <button
                        @click="loadQaHistory(qaHistoryPagination.page + 1)"
                        :disabled="qaHistoryPagination.page >= qaHistoryPagination.totalPages - 1"
                        class="h-8 sm:h-9 px-2 sm:px-3 rounded-lg border border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-400 text-xs sm:text-sm font-medium hover:bg-slate-100 dark:hover:bg-slate-800 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-1"
                      >
                        <span class="hidden xs:inline">下一页</span>
                        <span class="material-symbols-outlined text-base sm:text-lg">chevron_right</span>
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Document Access Tab -->
              <div v-if="activeTab === 'documents'">
                <!-- Loading State with Skeleton -->
                <div v-if="tabLoading" class="space-y-3">
                  <div class="hidden sm:grid grid-cols-3 gap-4 pb-3 border-b border-slate-200 dark:border-slate-700">
                    <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded animate-pulse"></div>
                    <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded animate-pulse"></div>
                    <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded animate-pulse"></div>
                  </div>
                  <div v-for="i in 5" :key="i" class="py-3 border-b border-slate-200 dark:border-slate-700 last:border-b-0 animate-pulse">
                    <div class="hidden sm:grid grid-cols-3 gap-4 items-center">
                      <div class="flex items-center gap-2">
                        <div class="w-5 h-5 bg-slate-200 dark:bg-slate-700 rounded"></div>
                        <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-32"></div>
                      </div>
                      <div class="h-6 bg-slate-200 dark:bg-slate-700 rounded-full w-10"></div>
                      <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-28"></div>
                    </div>
                    <div class="sm:hidden space-y-2">
                      <div class="h-4 bg-slate-200 dark:bg-slate-700 rounded w-3/4"></div>
                      <div class="h-3 bg-slate-200 dark:bg-slate-700 rounded w-1/2"></div>
                    </div>
                  </div>
                </div>

                <!-- Empty State -->
                <div v-else-if="documentAccess.length === 0" class="text-center py-8 sm:py-12">
                  <span class="material-symbols-outlined text-3xl sm:text-4xl text-slate-300 dark:text-slate-600">description</span>
                  <p class="text-slate-500 dark:text-slate-400 mt-3 text-sm sm:text-base">暂无文档访问记录</p>
                </div>

                <!-- Document Access - Desktop Table -->
                <div v-else>
                  <div class="hidden sm:block overflow-x-auto">
                    <table class="w-full">
                      <thead>
                        <tr class="border-b border-slate-200 dark:border-slate-700">
                          <th class="px-3 lg:px-4 py-2 lg:py-3 text-left text-xs lg:text-sm font-semibold text-slate-900 dark:text-white">文档标题</th>
                          <th class="px-3 lg:px-4 py-2 lg:py-3 text-left text-xs lg:text-sm font-semibold text-slate-900 dark:text-white">访问次数</th>
                          <th class="px-3 lg:px-4 py-2 lg:py-3 text-left text-xs lg:text-sm font-semibold text-slate-900 dark:text-white">最后访问时间</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr
                          v-for="doc in documentAccess"
                          :key="doc.documentId"
                          class="border-b border-slate-200 dark:border-slate-700 last:border-b-0 hover:bg-slate-50 dark:hover:bg-slate-800/30 transition-colors"
                        >
                          <td class="px-3 lg:px-4 py-2 lg:py-3">
                            <div class="flex items-center gap-2">
                              <span class="material-symbols-outlined text-slate-400 text-base lg:text-lg">description</span>
                              <span class="text-xs lg:text-sm text-slate-900 dark:text-white truncate max-w-[200px]">{{ doc.documentTitle }}</span>
                            </div>
                          </td>
                          <td class="px-3 lg:px-4 py-2 lg:py-3">
                            <span class="inline-flex items-center px-2 py-0.5 lg:px-2.5 lg:py-1 rounded-full text-xs font-medium bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400">
                              {{ doc.accessCount }}
                            </span>
                          </td>
                          <td class="px-3 lg:px-4 py-2 lg:py-3 text-xs lg:text-sm text-slate-500 dark:text-slate-400 whitespace-nowrap">
                            {{ formatDate(doc.lastAccessAt) }}
                          </td>
                        </tr>
                      </tbody>
                    </table>
                  </div>

                  <!-- Document Access - Mobile Card View -->
                  <div class="sm:hidden divide-y divide-slate-200 dark:divide-slate-700">
                    <div
                      v-for="doc in documentAccess"
                      :key="doc.documentId"
                      class="py-3 first:pt-0 last:pb-0"
                    >
                      <div class="flex items-start gap-2">
                        <span class="material-symbols-outlined text-slate-400 text-lg flex-shrink-0 mt-0.5">description</span>
                        <div class="flex-1 min-w-0">
                          <p class="text-sm text-slate-900 dark:text-white truncate">{{ doc.documentTitle }}</p>
                          <div class="flex items-center gap-3 mt-1">
                            <span class="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400">
                              {{ doc.accessCount }} 次
                            </span>
                            <span class="text-xs text-slate-400">{{ formatDate(doc.lastAccessAt) }}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>

                  <!-- Document Access Pagination - Responsive -->
                  <div
                    v-if="documentAccessPagination.totalPages > 1"
                    class="flex flex-col sm:flex-row items-center justify-between gap-3 pt-4 border-t border-slate-200 dark:border-slate-700 mt-4"
                  >
                    <p class="text-xs sm:text-sm text-slate-500 dark:text-slate-400 text-center sm:text-left">
                      共 {{ documentAccessPagination.totalElements }} 条记录，
                      第 {{ documentAccessPagination.page + 1 }} / {{ documentAccessPagination.totalPages }} 页
                    </p>
                    <div class="flex items-center gap-2">
                      <button
                        @click="loadDocumentAccess(documentAccessPagination.page - 1)"
                        :disabled="documentAccessPagination.page === 0"
                        class="h-8 sm:h-9 px-2 sm:px-3 rounded-lg border border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-400 text-xs sm:text-sm font-medium hover:bg-slate-100 dark:hover:bg-slate-800 transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-1"
                      >
                        <span class="material-symbols-outlined text-base sm:text-lg">chevron_left</span>
                        <span class="hidden xs:inline">上一页</span>
                      </button>
                      <button
                        @click="loadDocumentAccess(documentAccessPagination.page + 1)"
                        :disabled="documentAccessPagination.page >= documentAccessPagination.totalPages - 1"
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
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../../stores/user'
import { useTeacherStudentStore } from '../../stores/teacherStudent'
import { useToast } from '@/composables/useToast'
import TeacherSidebar from '../../components/teacher/TeacherSidebar.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const teacherStudentStore = useTeacherStudentStore()
const toast = useToast()

// State
const activeTab = ref('qa')
const tabLoading = ref(false)

// Computed properties from store
const loading = computed(() => teacherStudentStore.loading)
const error = computed(() => teacherStudentStore.error)
const currentStudent = computed(() => teacherStudentStore.currentStudent)
const qaHistory = computed(() => teacherStudentStore.qaHistory)
const qaHistoryPagination = computed(() => teacherStudentStore.qaHistoryPagination)
const documentAccess = computed(() => teacherStudentStore.documentAccess)
const documentAccessPagination = computed(() => teacherStudentStore.documentAccessPagination)

// Get student ID from route
const studentId = computed(() => route.params.id)

// Load student data on mount
onMounted(async () => {
  // Check teacher access
  if (userStore.user.role !== 'TEACHER') {
    router.push('/')
    return
  }

  // Clear previous student data
  teacherStudentStore.clearCurrentStudent()

  // Load student detail
  const result = await teacherStudentStore.fetchStudentDetail(studentId.value)
  
  if (result.success) {
    // Load initial tab data
    await loadQaHistory(0)
  }
})

// Watch for tab changes
watch(activeTab, async (newTab) => {
  if (newTab === 'qa' && qaHistory.value.length === 0) {
    await loadQaHistory(0)
  } else if (newTab === 'documents' && documentAccess.value.length === 0) {
    await loadDocumentAccess(0)
  }
})

// Load QA history
async function loadQaHistory(page) {
  tabLoading.value = true
  await teacherStudentStore.fetchStudentQaHistory(studentId.value, { page })
  tabLoading.value = false
}

// Load document access records
async function loadDocumentAccess(page) {
  tabLoading.value = true
  await teacherStudentStore.fetchStudentDocumentAccess(studentId.value, page)
  tabLoading.value = false
}

// Navigate back to student list
function goBack() {
  router.push('/teacher/students')
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
