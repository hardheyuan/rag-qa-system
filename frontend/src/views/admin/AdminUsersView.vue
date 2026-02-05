<template>
  <div class="h-screen flex flex-row bg-background-light dark:bg-background-dark overflow-hidden">
    <!-- Sidebar -->
    <AdminSidebar class="h-full self-stretch" />
    
    <!-- Main Content -->
    <main class="flex-1 flex flex-col min-w-0 overflow-hidden">
      <!-- Header -->
      <header class="px-8 py-6 border-b border-slate-200 dark:border-slate-800">
        <h1 class="text-2xl font-bold text-slate-900 dark:text-white">用户管理</h1>
        <p class="text-slate-500 dark:text-slate-400 mt-1">管理平台用户账号、角色分配和权限控制</p>
      </header>

      <!-- Content Area -->
      <div class="flex-1 overflow-auto p-8">
        <div class="space-y-6">
          <!-- Stats Cards -->
          <div class="grid grid-cols-4 gap-4">
            <div class="bg-white dark:bg-[#1a222c] rounded-lg p-4 border border-slate-200 dark:border-slate-800">
              <p class="text-slate-500 dark:text-slate-400 text-sm">总用户数</p>
              <p class="text-2xl font-bold text-slate-900 dark:text-white mt-1">{{ usersStore.userStats.total }}</p>
            </div>
            <div class="bg-white dark:bg-[#1a222c] rounded-lg p-4 border border-slate-200 dark:border-slate-800">
              <p class="text-slate-500 dark:text-slate-400 text-sm">管理员</p>
              <p class="text-2xl font-bold text-primary mt-1">{{ usersStore.userStats.admin }}</p>
            </div>
            <div class="bg-white dark:bg-[#1a222c] rounded-lg p-4 border border-slate-200 dark:border-slate-800">
              <p class="text-slate-500 dark:text-slate-400 text-sm">教师</p>
              <p class="text-2xl font-bold text-blue-500 mt-1">{{ usersStore.userStats.teacher }}</p>
            </div>
            <div class="bg-white dark:bg-[#1a222c] rounded-lg p-4 border border-slate-200 dark:border-slate-800">
              <p class="text-slate-500 dark:text-slate-400 text-sm">学生</p>
              <p class="text-2xl font-bold text-green-500 mt-1">{{ usersStore.userStats.student }}</p>
            </div>
          </div>

          <!-- Search and Filter Bar -->
          <div class="flex flex-wrap items-center gap-4">
            <!-- Search Input -->
            <div class="flex-1 min-w-[300px]">
              <div class="relative">
                <span class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-slate-400">
                  search
                </span>
                <input
                  v-model="searchQuery"
                  type="text"
                  placeholder="搜索用户姓名或邮箱..."
                  class="w-full h-11 pl-12 pr-4 rounded-lg border border-slate-200 dark:border-slate-700 bg-white dark:bg-[#1a222c] text-slate-900 dark:text-white placeholder:text-slate-400 focus:outline-none focus:border-primary transition-all"
                  @input="handleSearch"
                />
              </div>
            </div>

            <!-- Role Filters -->
            <div class="flex items-center gap-2">
              <button
                v-for="role in roleFilters"
                :key="role.value"
                @click="setRoleFilter(role.value)"
                :class="[
                  'h-9 px-4 rounded-lg text-sm font-medium transition-all',
                  usersStore.roleFilter === role.value
                    ? 'bg-primary text-white'
                    : 'bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400 hover:bg-slate-200 dark:hover:bg-slate-700'
                ]"
              >
                {{ role.label }}
              </button>
            </div>

            <!-- Create User Button -->
            <button
              @click="showCreateModal = true"
              class="h-9 px-4 rounded-lg bg-primary text-white text-sm font-medium hover:bg-blue-600 transition-all flex items-center gap-2"
            >
              <span class="material-symbols-outlined text-lg">add</span>
              新建用户
            </button>
          </div>

          <!-- Users Table -->
          <div class="bg-white dark:bg-[#1a222c] rounded-lg border border-slate-200 dark:border-slate-800 overflow-hidden">
            <table class="w-full">
              <thead>
                <tr class="bg-slate-50 dark:bg-slate-800/50 border-b border-slate-200 dark:border-slate-700">
                  <th class="px-6 py-4 text-left text-sm font-semibold text-slate-900 dark:text-white">姓名</th>
                  <th class="px-6 py-4 text-left text-sm font-semibold text-slate-900 dark:text-white">邮箱</th>
                  <th class="px-6 py-4 text-left text-sm font-semibold text-slate-900 dark:text-white">角色</th>
                  <th class="px-6 py-4 text-left text-sm font-semibold text-slate-900 dark:text-white">状态</th>
                  <th class="px-6 py-4 text-left text-sm font-semibold text-slate-900 dark:text-white">最后登录</th>
                  <th class="px-6 py-4 text-left text-sm font-semibold text-slate-900 dark:text-white">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="user in usersStore.filteredUsers"
                  :key="user.id"
                  class="border-b border-slate-200 dark:border-slate-700 last:border-b-0 hover:bg-slate-50 dark:hover:bg-slate-800/30 transition-colors"
                >
                  <td class="px-6 py-4">
                    <div class="flex items-center gap-3">
                      <div class="w-9 h-9 rounded-full bg-slate-200 dark:bg-slate-700 flex items-center justify-center">
                        <span class="material-symbols-outlined text-slate-500 text-lg">person</span>
                      </div>
                      <span class="text-sm font-medium text-slate-900 dark:text-white">{{ user.name }}</span>
                    </div>
                  </td>
                  <td class="px-6 py-4 text-sm text-slate-600 dark:text-slate-400">{{ user.email }}</td>
                  <td class="px-6 py-4">
                    <span
                      :class="[
                        'inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium',
                        getRoleClass(user.role)
                      ]"
                    >
                      {{ getRoleLabel(user.role) }}
                    </span>
                  </td>
                  <td class="px-6 py-4">
                    <span
                      :class="[
                        'inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium',
                        user.status === 'Active'
                          ? 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400'
                          : 'bg-red-100 dark:bg-red-900/30 text-red-700 dark:text-red-400'
                      ]"
                    >
                      {{ user.status === 'Active' ? '活跃' : '停用' }}
                    </span>
                  </td>
                  <td class="px-6 py-4 text-sm text-slate-500 dark:text-slate-400">{{ formatDate(user.lastLogin) }}</td>
                  <td class="px-6 py-4">
                    <div class="flex items-center gap-2">
                      <button
                        @click="editUser(user)"
                        class="p-2 rounded-lg text-slate-500 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 hover:text-primary transition-all"
                        title="编辑"
                      >
                        <span class="material-symbols-outlined text-lg">edit</span>
                      </button>
                      <button
                        @click="confirmDelete(user)"
                        class="p-2 rounded-lg text-slate-500 dark:text-slate-400 hover:bg-red-50 dark:hover:bg-red-900/20 hover:text-red-500 transition-all"
                        title="删除"
                      >
                        <span class="material-symbols-outlined text-lg">delete</span>
                      </button>
                    </div>
                  </td>
                </tr>
                <tr v-if="usersStore.filteredUsers.length === 0">
                  <td colspan="6" class="px-6 py-12 text-center">
                    <div class="flex flex-col items-center gap-3">
                      <span class="material-symbols-outlined text-4xl text-slate-300 dark:text-slate-600">search_off</span>
                      <p class="text-slate-500 dark:text-slate-400">未找到匹配的用户</p>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </main>

    <!-- Create User Modal -->
    <UserCreateModal
      v-if="showCreateModal"
      @close="showCreateModal = false"
      @create="handleCreateUser"
    />

    <!-- Edit User Modal -->
    <UserEditModal
      v-if="editingUser"
      :user="editingUser"
      @close="editingUser = null"
      @save="handleUpdateUser"
    />

    <!-- Delete Confirmation Modal -->
    <div
      v-if="deletingUser"
      class="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
      @click="deletingUser = null"
    >
      <div class="bg-white dark:bg-[#1a222c] rounded-lg p-6 max-w-md w-full mx-4" @click.stop>
        <div class="flex items-center gap-3 mb-4">
          <div class="w-10 h-10 rounded-full bg-red-100 dark:bg-red-900/30 flex items-center justify-center">
            <span class="material-symbols-outlined text-red-600">warning</span>
          </div>
          <h3 class="text-lg font-semibold text-slate-900 dark:text-white">确认删除</h3>
        </div>
        <p class="text-slate-600 dark:text-slate-400 mb-6">
          确定要删除用户 <span class="font-semibold text-slate-900 dark:text-white">{{ deletingUser.name }}</span> 吗？此操作不可撤销。
        </p>
        <div class="flex justify-end gap-3">
          <button
            @click="deletingUser = null"
            class="px-4 py-2 rounded-lg text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 transition-all"
          >
            取消
          </button>
          <button
            @click="handleDeleteUser"
            class="px-4 py-2 rounded-lg bg-red-500 text-white hover:bg-red-600 transition-all"
          >
            删除
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
import { useUsersStore } from '../../stores/users'
import AdminSidebar from '../../components/admin/AdminSidebar.vue'
import UserCreateModal from '../../components/admin/UserCreateModal.vue'
import UserEditModal from '../../components/admin/UserEditModal.vue'

const router = useRouter()
const userStore = useUserStore()
const usersStore = useUsersStore()

// Search and filter
const searchQuery = ref('')
const roleFilters = [
  { value: 'All', label: '全部' },
  { value: 'Admin', label: '管理员' },
  { value: 'Teacher', label: '教师' },
  { value: 'Student', label: '学生' }
]

// Modals
const showCreateModal = ref(false)
const editingUser = ref(null)
const deletingUser = ref(null)

// Check admin access
onMounted(() => {
  if (userStore.user.role !== 'Admin') {
    router.push('/')
    return
  }
  usersStore.init()
})

// Search handler with debounce
let searchTimeout
function handleSearch() {
  clearTimeout(searchTimeout)
  searchTimeout = setTimeout(() => {
    usersStore.setSearchQuery(searchQuery.value)
  }, 300)
}

function setRoleFilter(role) {
  usersStore.setRoleFilter(role)
}

function getRoleClass(role) {
  switch (role) {
    case 'Admin':
      return 'bg-purple-100 dark:bg-purple-900/30 text-purple-700 dark:text-purple-400'
    case 'Teacher':
      return 'bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400'
    case 'Student':
      return 'bg-green-100 dark:bg-green-900/30 text-green-700 dark:text-green-400'
    default:
      return 'bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-400'
  }
}

function getRoleLabel(role) {
  switch (role) {
    case 'Admin':
      return '管理员'
    case 'Teacher':
      return '教师'
    case 'Student':
      return '学生'
    default:
      return role
  }
}

function formatDate(dateString) {
  if (dateString === '-') return '-'
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

function editUser(user) {
  editingUser.value = { ...user }
}

function confirmDelete(user) {
  deletingUser.value = user
}

async function handleCreateUser(userData) {
  try {
    await usersStore.createUser(userData)
    showCreateModal.value = false
    // 可以添加成功提示
    console.log('用户创建成功')
  } catch (error) {
    // 显示更详细的错误信息
    const errorMessage = error.response?.data?.message || error.message || '创建用户失败'
    alert('创建用户失败：' + errorMessage)
  }
}

async function handleUpdateUser(userData) {
  try {
    await usersStore.updateUser(userData.id, userData)
    editingUser.value = null
  } catch (error) {
    alert('更新用户失败：' + error.message)
  }
}

async function handleDeleteUser() {
  if (!deletingUser.value) return
  try {
    await usersStore.deleteUser(deletingUser.value.id)
    deletingUser.value = null
  } catch (error) {
    alert('删除用户失败：' + error.message)
  }
}
</script>
