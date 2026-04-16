import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'
import HomeView from '../views/HomeView.vue'
import ProfileView from '../views/ProfileView.vue'
import MenuPlaceholderView from '../views/MenuPlaceholderView.vue'
import CoreIndicatorsView from '../views/CoreIndicatorsView.vue'
import CarbonEmissionChartsView from '../views/CarbonEmissionChartsView.vue'
import ShipmentTrackingView from '../views/ShipmentTrackingView.vue'
import GenerateReportView from '../views/GenerateReportView.vue'
import SupplierManagementView from '../views/SupplierManagementView.vue'
import SupplierChainMapView from '../views/SupplierChainMapView.vue'
import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import ResetPasswordView from '../views/ResetPasswordView.vue'

function isLoggedIn() {
  try {
    return Boolean(sessionStorage.getItem('greenchain_user'))
  } catch {
    return false
  }
}

const menuMeta = { showAppMenu: true }

const routes = [
  { path: '', name: 'home', component: HomeView, meta: { ...menuMeta } },
  { path: 'profile', name: 'profile', component: ProfileView, meta: { ...menuMeta } },
  {
    path: 'core-indicators',
    name: 'core-indicators',
    component: CoreIndicatorsView,
    meta: { ...menuMeta, menuTitle: 'Core indicators' },
  },
  {
    path: 'carbon-emission-charts',
    name: 'carbon-emission-charts',
    component: CarbonEmissionChartsView,
    meta: { ...menuMeta, menuTitle: 'Carbon Emission Charts' },
  },
  {
    path: 'supplier-chain-map',
    name: 'supplier-chain-map',
    component: SupplierChainMapView,
    meta: { ...menuMeta, menuTitle: 'Supplier Chain Map' },
  },
  {
    path: 'supplier-management',
    name: 'supplier-management',
    component: SupplierManagementView,
    meta: { ...menuMeta, menuTitle: 'Supplier Management' },
  },
  {
    path: 'shipment-tracking',
    name: 'shipment-tracking',
    component: ShipmentTrackingView,
    meta: { ...menuMeta, menuTitle: 'Shipment Tracking' },
  },
  {
    path: 'generate-report',
    name: 'generate-report',
    component: GenerateReportView,
    meta: { ...menuMeta, menuTitle: 'Generate Report' },
  },
  { path: 'login', name: 'login', component: LoginView, meta: { showAppMenu: false } },
  { path: 'register', name: 'register', component: RegisterView, meta: { showAppMenu: false } },
  {
    path: 'reset-password',
    name: 'reset-password',
    component: ResetPasswordView,
    meta: { showAppMenu: false },
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: MainLayout,
      children: routes,
    },
  ],
})

router.beforeEach((to) => {
  if (to.name === 'profile' && !isLoggedIn()) {
    return { name: 'login' }
  }
})

export default router
