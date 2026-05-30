import { createRouter, createWebHistory } from 'vue-router';

const ReimburseList = () => import('@/views/ReimburseListView.vue');
const ReimburseDetail = () => import('@/views/ReimburseDetailView.vue');

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/reimburse',
    },
    {
      path: '/reimburse',
      name: 'ReimburseList',
      component: ReimburseList,
      meta: { title: '报销单列表' },
    },
    {
      path: '/reimburse/:id',
      name: 'ReimburseDetail',
      component: ReimburseDetail,
      meta: { title: '差旅费用报销单' },
    },
  ],
});

router.afterEach((to) => {
  document.title = `${String(to.meta.title ?? '差旅报销单系统')} - 差旅报销单系统`;
});

export default router;
