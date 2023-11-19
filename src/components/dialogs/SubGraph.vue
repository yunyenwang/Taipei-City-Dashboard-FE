<!-- Developed by Taipei Urban Intelligence Center 2023 -->

<!-- This component only serves a functional purpose if a backend is connected -->
<!-- For static applications, this component could be removed or modified to be a dashboard component overviewer -->

<script setup>
import { ref, watch } from "vue";
import { useDialogStore } from "../../store/dialogStore";
import axios from "axios";
import DialogContainer from "./DialogContainer.vue";
import { useContentStore } from '../../store/contentStore';
const { BASE_URL } = import.meta.env;


const contentStore = useContentStore();

const props = defineProps(['district']);
const selectedDistrict = ref(props.district);

var averageData = ref(null);
var rangeData = ref(null)

const dialogStore = useDialogStore();

var series = ref([]);

function handleClose() {
	series.value = []
	dialogStore.hideAllDialogs();
}
watch(dialogStore.dialogs.subGraph, async (newValue) => {
	if (newValue) {
		getData()
	}
})
watch(props, async (newValue) => {
	// console.log('selectedDistrict', newValue)
	getData()
	// console.log('series', series.value)
});

async function getData() {
	await axios
	.get(`${BASE_URL}/chartData/floodData/average/${props.district}積水均值.json`)
	.then((rs) => {
		averageData = rs.data
		// console.log('averageData', averageData)
	})
	.catch((e) => {
		console.error(e);
	});

	await axios
	.get(`${BASE_URL}/chartData/floodData/range/${props.district}積水最高值與最低值.json`)
	.then((rs) => {
		rangeData = rs.data
		// console.log('rangeData', rangeData)
	})
	.catch((e) => {
		console.error(e);
	});

	series.value = [averageData, rangeData];
}


const chartOptions = ref({
	chart: {
		height: 500,
		type: "rangeArea",
		zoom: {
			enabled: true,
		}
	},
	colors: ['#33b2df', '#CAF0F8'],
	dataLabels: {
		enabled: false,
	},
	fill: {
		opacity: [1, 0.5]
	},
	forecastDataPoints: {
		count: 2
	},
	stroke: {
		curve: "straight",
		width: [2, 0]
	},
	title: {
		text: "歷年積水深度",
		align: "left",
	},
	grid: {
		row: {
			//colors: ["#f3f3f3", "transparent"], // takes an array which will be repeated on columns
			opacity: 0.5,
		},
	},
	markers: {
		hover: {
		sizeOffset: 5
		}
	},
	tooltip: {
		theme: "dark",
	},
	xaxis: {
		type: 'datetime',
		labels: {
			datetimeUTC: false,
			formatter: function (value, timestamp) {
				const date = new Date(value);
				const year = date.getFullYear();
				const month = (date.getMonth() + 1).toString().padStart(2, '0');
				const day = date.getDate().toString().padStart(2, '0');
				return `${year}/${month}/${day}`;
			}
		}
	}
});
</script>

<template>
	<!-- v-if="contentStore.currentDashboard.mode !== '/mapview'"  -->
	<DialogContainer v-if="contentStore.currentDashboard.mode !== '/mapview'" :dialog="`subGraph`" @onClose="handleClose">
		
		<h2>{{ props.district }}</h2>
		<div id="chart">
			<apexchart
				v-show="series.length > 0"
				height="400"
				width="400"
				:options="chartOptions"
				:series="series"
			></apexchart>
		</div>
	</DialogContainer>
</template>

<style scoped lang="scss"></style>
