<!-- Developed by Taipei Urban Intelligence Center 2023 -->

<script setup>
import { ref } from 'vue';
import { useMapStore } from '../../store/mapStore';
import { useContentStore } from '../../store/contentStore';

const props = defineProps(['chart_config', 'activeChart', 'series', 'map_config']);
const mapStore = useMapStore();
const contentStore = useContentStore();

const chartOptions = ref({
	chart: {
		stacked: false,
		toolbar: {
			show: false
		},
	},
	colors: props.chart_config.color,
	dataLabels: {
		enabled: false
	},
	grid: {
		show: false,
	},
	legend: {
		show: props.chart_config.categories ? true : false,
	},
	plotOptions: {
		bar: {
			columnWidth: "20%"
		}
	},
	stroke: {  
		width: [4, 4, 4]
	},
	tooltip: {
		// The class "chart-tooltip" could be edited in /assets/styles/chartStyles.css
		custom: function ({ series, seriesIndex, dataPointIndex, w }) {
			return '<div class="chart-tooltip">' +
				'<h6>' + w.globals.labels[dataPointIndex] + `${props.chart_config.categories ? '-' + w.globals.seriesNames[seriesIndex] : ''}` + '</h6>' +
				'<span>' + series[seriesIndex][dataPointIndex] + ` ${props.chart_config.unit}` + '</span>' +
				'</div>';
		},
	},
	xaxis: {
		axisBorder: {
			show: false,
		},
		axisTicks: {
			show: false,
		},
		categories: props.chart_config.categories ? props.chart_config.categories : [],
		labels: {
			offsetY: 5,
		},
		type: 'category',
	},
	yaxis: [
		{
			seriesName: 'Column A',
			axisTicks: {
				show: false
			},
			axisBorder: {
				show: false,
			},
			title: {
				text: "xx統計值 (column)"
			}
		},
		{
			seriesName: 'Column A',
			show: false
		}, 
		{
			opposite: true,
			seriesName: 'Line C',
			axisTicks: {
				show: false
			},
			axisBorder: {
				show: false,
			},
			title: {
				text: "xx率 (line)"
			}
		}
	],

	
});

const selectedIndex = ref(null);

// function handleDataSelection(e, chartContext, config) {
// 	if (!props.chart_config.map_filter) {
// 		return;
// 	}
// 	const toFilter = config.dataPointIndex;
// 	if (toFilter !== selectedIndex.value) {
// 		mapStore.addLayerFilter(`${props.map_config[0].index}-${props.map_config[0].type}`, props.chart_config.map_filter[0], props.chart_config.map_filter[1][toFilter]);
// 		selectedIndex.value = toFilter;
// 	} else {
// 		mapStore.clearLayerFilter(`${props.map_config[0].index}-${props.map_config[0].type}`);
// 		selectedIndex.value = null;
// 	}
// }
</script>

<template>
	<div v-if="activeChart === 'MultipleYAxisChart'">
		<apexchart width="100%" height="270px" type="bar" :options="chartOptions" :series="series"
			@dataPointSelection="handleDataSelection"></apexchart>
	</div>
</template>