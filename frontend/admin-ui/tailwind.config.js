/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          DEFAULT: '#0f1d2c',
          accent: '#1f6feb',
        },
      },
    },
  },
  plugins: [],
};
