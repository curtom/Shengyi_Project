import type { Config } from 'tailwindcss';

export default {
  content: ['./index.html', './src/**/*.{vue,ts,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: '#008dd6',
        'page-bg': '#f5f6f8',
        'section-head': '#f0f2f5',
      },
    },
  },
  plugins: [],
} satisfies Config;
